const express=require("express"),http=require("http"),WebSocket=require("ws"),path=require("path"),crypto=require("crypto"),fs=require("fs");
const app=express(),server=http.createServer(app),wss=new WebSocket.Server({server});
const boats=new Map(),tracks=new Map(),sessions=new Map(),regattas=new Map(),marks=new Map(),races=new Map(),raceTracks=new Map();
const DATA_FILE=process.env.OPTILIVE_DATA_FILE||"/data/optilive.json";
function persist(){try{fs.mkdirSync(path.dirname(DATA_FILE),{recursive:true});const tmp=DATA_FILE+".tmp";fs.writeFileSync(tmp,JSON.stringify({regattas:[...regattas.entries()],marks:[...marks.entries()],races:[...races.entries()],raceTracks:[...raceTracks.entries()]}));fs.renameSync(tmp,DATA_FILE)}catch(e){console.error("persist",e.message)}}
function restore(){try{if(!fs.existsSync(DATA_FILE))return;const d=JSON.parse(fs.readFileSync(DATA_FILE,"utf8"));for(const x of d.regattas||[])regattas.set(x[0],x[1]);for(const x of d.marks||[])marks.set(x[0],x[1]);for(const x of d.races||[])races.set(x[0],x[1]);for(const x of d.raceTracks||[])raceTracks.set(x[0],x[1]);console.log("OptiLive data restored",regattas.size,marks.size,races.size)}catch(e){console.error("restore",e.message)}}
restore();
app.use(express.json({limit:"256kb"}));app.use("/vendor/leaflet",express.static(path.join(__dirname,"node_modules","leaflet","dist")));app.use(express.static(path.join(__dirname,"public")));
const ADMIN_USER=process.env.OPTILIVE_ADMIN_USER||"admin";
const ADMIN_HASH=process.env.OPTILIVE_ADMIN_PASSWORD_HASH||"";
const ADMIN_PASSWORD=process.env.OPTILIVE_ADMIN_PASSWORD||"";
function safeEq(a,b){const A=Buffer.from(String(a)),B=Buffer.from(String(b));return A.length===B.length&&crypto.timingSafeEqual(A,B)}
function verifyPassword(password){if(ADMIN_HASH){const [salt,hash]=ADMIN_HASH.split(":");if(!salt||!hash)return false;const got=crypto.scryptSync(password,salt,64).toString("hex");return safeEq(got,hash)}return ADMIN_PASSWORD&&safeEq(password,ADMIN_PASSWORD)}
const SESSION_FILE=DATA_FILE+".sessions";
const credentialVersion=crypto.createHash("sha256").update(ADMIN_USER+":"+ADMIN_HASH+":"+ADMIN_PASSWORD).digest("hex");
const sessionKey=t=>crypto.createHash("sha256").update(t).digest("hex");
function persistSessions(){fs.mkdirSync(path.dirname(SESSION_FILE),{recursive:true});const tmp=SESSION_FILE+".tmp";fs.writeFileSync(tmp,JSON.stringify({credentialVersion,sessions:[...sessions].filter(([,v])=>v.expires>Date.now())}),{mode:0o600});fs.renameSync(tmp,SESSION_FILE)}
try{const d=JSON.parse(fs.readFileSync(SESSION_FILE,"utf8"));if(d.credentialVersion===credentialVersion)for(const [k,v] of d.sessions||[])if(v.expires>Date.now())sessions.set(k,v)}catch(e){if(e.code!=="ENOENT")console.error("session restore failed")}
function auth(req,res,next){const h=req.headers.authorization||"";const t=h.startsWith("Bearer ")?h.slice(7):"";const s=sessions.get(sessionKey(t));if(!s||s.expires<Date.now()){if(t)sessions.delete(sessionKey(t));return res.status(401).json({ok:false,error:"unauthorized"})}req.user=s;next()}
app.get("/api/health",(_q,r)=>r.json({ok:true,boats:boats.size,time:Date.now()}));
app.post("/api/auth/login",(req,res)=>{const {username,password}=req.body||{};if(username!==ADMIN_USER||!verifyPassword(String(password||"")))return res.status(401).json({ok:false,error:"invalid_credentials"});const token=crypto.randomBytes(32).toString("hex");sessions.set(sessionKey(token),{username,role:"admin",expires:Date.now()+12*60*60*1000});persistSessions();res.json({ok:true,token,role:"admin",expiresIn:43200})});
app.post("/api/auth/logout",auth,(req,res)=>{const h=req.headers.authorization||"";sessions.delete(sessionKey(h.slice(7)));persistSessions();res.json({ok:true})});
app.get("/api/regattas/current",(_q,res)=>{const active=[...races.values()].filter(r=>r.status==="En curso"&&regattas.has(r.regattaId)).sort((a,b)=>(b.startedAt||b.updatedAt||0)-(a.startedAt||a.updatedAt||0));const race=active[0]||null;res.json({regatta:race?regattas.get(race.regattaId):null,race})});
app.get("/api/regattas",(_q,r)=>r.json([...regattas.values()]));
app.post("/api/admin/regattas",auth,(req,res)=>{const {name,club,venue,startDate,endDate,days}=req.body||{};if(!name)return res.status(400).json({ok:false,error:"name required"});const id=String(regattas.size+1).padStart(2,"0");const item={id,name:String(name),club:String(club||""),venue:String(venue||""),startDate:startDate||null,endDate:endDate||null,days:Array.isArray(days)?days:[],status:"draft",createdAt:Date.now()};regattas.set(id,item);persist();res.status(201).json(item)});
app.put("/api/admin/regattas/:id",auth,(req,res)=>{const old=regattas.get(req.params.id);if(!old)return res.status(404).json({ok:false,error:"not_found"});const item={...old,...req.body,id:old.id,updatedAt:Date.now()};regattas.set(old.id,item);persist();res.json(item)});
app.post("/api/admin/regattas/:id/marks",auth,(req,res)=>{if(!regattas.has(req.params.id))return res.status(404).json({ok:false,error:"regatta_not_found"});const {deviceId,name,type,lat,lon}=req.body||{};const hasCoords=Number.isFinite(lat)&&Number.isFinite(lon)&&lat>=-90&&lat<=90&&lon>=-180&&lon<=180;if(!name||(!deviceId&&!hasCoords))return res.status(400).json({ok:false,error:"name and GPS or coordinates required"});const dev=deviceId?String(deviceId):("MANUAL-"+crypto.randomUUID());const key=req.params.id+":"+dev;const m={regattaId:req.params.id,deviceId:dev,name:String(name),type:String(type||"mark"),lat:hasCoords?lat:null,lon:hasCoords?lon:null,positionSource:deviceId?"gps":"manual",updatedAt:Date.now()};marks.set(key,m);persist();res.status(201).json(m)});
app.put("/api/admin/regattas/:id/marks/:deviceId",auth,(req,res)=>{const key=req.params.id+":"+req.params.deviceId,m=marks.get(key);if(!m)return res.status(404).json({ok:false,error:"mark_not_found"});const {lat,lon}=req.body||{};if(!Number.isFinite(lat)||!Number.isFinite(lon)||lat < -90||lat > 90||lon < -180||lon > 180)return res.status(400).json({ok:false,error:"valid coordinates required"});Object.assign(m,{lat,lon,positionSource:"manual",updatedAt:Date.now()});marks.set(key,m);persist();const msg=JSON.stringify({type:"mark_position",data:m});for(const c of wss.clients)if(c.readyState===WebSocket.OPEN)c.send(msg);res.json(m)});
app.delete("/api/admin/regattas/:id/marks/:deviceId",auth,(req,res)=>{const key=req.params.id+":"+req.params.deviceId,m=marks.get(key);if(!m)return res.status(404).json({ok:false,error:"mark_not_found"});marks.delete(key);persist();const msg=JSON.stringify({type:"mark_deleted",data:{regattaId:m.regattaId,deviceId:m.deviceId}});for(const c of wss.clients)if(c.readyState===WebSocket.OPEN)c.send(msg);res.json({ok:true})});
app.post("/api/admin/regattas/:id/races",auth,(req,res)=>{
 const id=req.params.id;if(!regattas.has(id))return res.status(404).json({error:"regatta_not_found"});
 const {day,number,status,date,plannedTime}=req.body||{};
 if(!Number.isInteger(day)||day<1||!Number.isInteger(number)||number<1)return res.status(400).json({error:"positive day and number required"});
 const key=id+":"+day+":"+number,old=races.get(key)||{},st=status||old.status||"Pendiente",now=Date.now();
 if(!["Pendiente","Preparada","En curso","Finalizada","Aplazada","Anulada"].includes(st))return res.status(400).json({error:"invalid_status"});
 if(old.startedAt&&st!==old.status&&st!=="Finalizada"&&st!=="Anulada")return res.status(409).json({error:"started_race_locked"});
 if(st==="En curso"&&[...races.entries()].some(([k,r])=>k!==key&&r.regattaId===id&&r.status==="En curso"))return res.status(409).json({error:"finish_active_race_first"});
 const item={...old,regattaId:id,day,number,status:st,updatedAt:now};
 if(date!==undefined)item.date=date;if(plannedTime!==undefined)item.plannedTime=plannedTime;
 if(st==="En curso"&&!item.startedAt){item.startedAt=now;item.marks=item.marks||[...marks.values()].filter(m=>m.regattaId===id).map(m=>({...m}));}
 if((st==="Finalizada"||st==="Anulada")&&item.startedAt&&!item.endedAt)item.endedAt=now;
 races.set(key,item);persist();res.status(201).json(item);
});
app.post("/api/admin/regattas/:id/schedule",auth,(req,res)=>{
 const id=req.params.id,g=regattas.get(id),days=req.body.days;
 if(!g)return res.status(404).json({error:"regatta_not_found"});
 if(!Array.isArray(days)||!days.length||days.length>30||days.some(d=>!/^\d{4}-\d{2}-\d{2}$/.test(d.date)||!Number.isInteger(d.count)||d.count<1||d.count>20||isNaN(Date.parse(d.date))))return res.status(400).json({error:"invalid_days"});
 if(new Set(days.map(d=>d.date)).size!==days.length)return res.status(400).json({error:"duplicate_dates"});
 if([...races.values()].some(r=>r.regattaId===id))return res.status(409).json({error:"schedule_already_exists"});
 let number=0;days.sort((a,b)=>a.date.localeCompare(b.date)).forEach((d,i)=>{for(let n=0;n<d.count;n++){number++;races.set(id+":"+(i+1)+":"+number,{regattaId:id,day:i+1,number,date:d.date,plannedTime:n===0?d.time||"":"",status:"Pendiente",marks:[],updatedAt:Date.now()});}});
 g.days=days;persist();res.status(201).json({ok:true,count:number});
});
app.get("/api/regattas/:id/races/:day/:number/marks",(req,res)=>{const r=races.get(req.params.id+":"+req.params.day+":"+req.params.number);if(!r)return res.status(404).json({error:"race_not_found"});res.json(r.marks||[])});
app.all("/api/admin/regattas/:id/races/:day/:number/marks/:deviceId?",auth,(req,res)=>{
 const key=req.params.id+":"+req.params.day+":"+req.params.number,r=races.get(key);
 if(!r)return res.status(404).json({error:"race_not_found"});if(r.startedAt||r.status==="Anulada")return res.status(409).json({error:"race_course_locked"});
 r.marks=r.marks||[];const b=req.body||{},index=r.marks.findIndex(m=>m.deviceId===req.params.deviceId);
 if(req.method==="DELETE"){if(index<0)return res.status(404).json({error:"mark_not_found"});r.marks.splice(index,1)}
 else if(req.method==="POST"||req.method==="PUT"){
 if(!Number.isFinite(b.lat)||!Number.isFinite(b.lon)||Math.abs(b.lat)>90||Math.abs(b.lon)>180)return res.status(400).json({error:"invalid_coordinates"});
 if(req.method==="PUT"&&index<0)return res.status(404).json({error:"mark_not_found"});if(req.method==="POST"&&!b.name)return res.status(400).json({error:"name_required"});
 const m={...(index>=0?r.marks[index]:{}),regattaId:r.regattaId,deviceId:req.params.deviceId||"MANUAL-"+crypto.randomUUID(),lat:b.lat,lon:b.lon,positionSource:"manual",updatedAt:Date.now()};
 if(b.name)m.name=String(b.name);if(b.type)m.type=String(b.type);if(index>=0)r.marks[index]=m;else r.marks.push(m);
 r.status="Pendiente";persist();return res.status(req.method==="POST"?201:200).json(m);
 }else return res.sendStatus(405);r.status="Pendiente";persist();res.json({ok:true});
});
app.post("/api/admin/regattas/:id/races/:day/:number/copy-course",auth,(req,res)=>{
 const key=req.params.id+":"+req.params.day+":"+req.params.number,r=races.get(key),source=races.get(req.params.id+":"+req.body.day+":"+req.body.number);
 if(!r||!source)return res.status(404).json({error:"race_not_found"});if(r.startedAt)return res.status(409).json({error:"race_course_locked"});
 r.marks=(source.marks||[]).map(m=>({...m}));r.status="Pendiente";persist();res.json({ok:true});
});
app.get("/api/regattas/:id/races",(req,res)=>res.json([...races.values()].filter(x=>x.regattaId===req.params.id).sort((a,b)=>a.day-b.day||a.number-b.number)));
app.get("/api/regattas/:id/marks",(req,res)=>res.json([...marks.values()].filter(x=>x.regattaId===req.params.id)));
app.post("/api/mark-position",(req,res)=>{const {regattaId,deviceId,lat,lon,accuracy,timestamp}=req.body||{};const key=String(regattaId)+":"+String(deviceId);const m=marks.get(key);if(!m||!Number.isFinite(lat)||!Number.isFinite(lon))return res.status(400).json({ok:false,error:"registered mark and coordinates required"});Object.assign(m,{lat,lon,positionSource:"gps",accuracy:Number.isFinite(accuracy)?accuracy:null,timestamp:Number.isFinite(timestamp)?timestamp:Date.now(),updatedAt:Date.now()});persist();const msg=JSON.stringify({type:"mark_position",data:m});for(const c of wss.clients)if(c.readyState===WebSocket.OPEN)c.send(msg);res.json({ok:true})});
app.get("/api/boats",(_q,r)=>r.json([...boats.values()]));app.get("/api/tracks",(_q,r)=>r.json(Object.fromEntries(tracks)));
app.get("/api/regattas/:id/races/:day/:number/tracks",(req,res)=>{const key=req.params.id+":"+req.params.day+":"+req.params.number;const race=races.get(key);if(!race)return res.status(404).json({error:"race_not_found"});res.json({race,marks:race.marks||[],tracks:raceTracks.get(key)||{}})});
app.post("/api/position",(req,res)=>{const {sail,lat,lon,accuracy,speedKnots,timestamp}=req.body||{};if(!sail||!Number.isFinite(lat)||!Number.isFinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180)return res.status(400).json({ok:false,error:"sail, lat and lon required"});const p={sail:String(sail).trim().toUpperCase(),lat,lon,accuracy:Number.isFinite(accuracy)?accuracy:null,speedKnots:Number.isFinite(speedKnots)?speedKnots:null,timestamp:Number.isFinite(timestamp)?timestamp:Date.now(),receivedAt:Date.now()};const candidates=[...races.entries()].filter(([k,r])=>r.startedAt&&p.timestamp>=r.startedAt&&(!r.endedAt||p.timestamp<=r.endedAt)&&(!req.body.regattaId||r.regattaId===String(req.body.regattaId)));if(candidates.length===1){const [key,r]=candidates[0];const all=raceTracks.get(key)||{};const t=all[p.sail]||[];if(!t.some(x=>x.timestamp===p.timestamp&&x.lat===p.lat&&x.lon===p.lon)){t.push(p);t.sort((a,b)=>a.timestamp-b.timestamp);all[p.sail]=t;raceTracks.set(key,all);persist();}p.regattaId=r.regattaId;p.day=r.day;p.number=r.number;}if(!boats.has(p.sail)||boats.get(p.sail).timestamp<=p.timestamp)boats.set(p.sail,p);if(!tracks.has(p.sail))tracks.set(p.sail,[]);const t=tracks.get(p.sail);t.push([p.lat,p.lon]);if(t.length>1500)t.shift();const msg=JSON.stringify({type:"position",data:p});for(const c of wss.clients)if(c.readyState===WebSocket.OPEN)c.send(msg);res.json({ok:true})});
wss.on("connection",ws=>ws.send(JSON.stringify({type:"snapshot",data:[...boats.values()],tracks:Object.fromEntries(tracks),marks:[...marks.values()]})));
const PORT=process.env.PORT||3000;server.listen(PORT,"0.0.0.0",()=>console.log("OptiLive server ready on "+PORT));