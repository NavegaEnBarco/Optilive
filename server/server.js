const express=require("express"),http=require("http"),WebSocket=require("ws"),path=require("path");
const app=express(),server=http.createServer(app),wss=new WebSocket.Server({server});
const boats=new Map(),tracks=new Map();
app.use(express.json({limit:"256kb"}));app.use(express.static(path.join(__dirname,"public")));
app.get("/api/health",(_q,r)=>r.json({ok:true,boats:boats.size,time:Date.now()}));
app.get("/api/boats",(_q,r)=>r.json([...boats.values()]));
app.get("/api/tracks",(_q,r)=>r.json(Object.fromEntries(tracks)));
app.post("/api/position",(req,res)=>{
 const {sail,lat,lon,accuracy,speedKnots,timestamp}=req.body||{};
 if(!sail||!Number.isFinite(lat)||!Number.isFinite(lon))return res.status(400).json({ok:false,error:"sail, lat and lon required"});
 const p={sail:String(sail).trim().toUpperCase(),lat,lon,accuracy:Number.isFinite(accuracy)?accuracy:null,speedKnots:Number.isFinite(speedKnots)?speedKnots:null,timestamp:Number.isFinite(timestamp)?timestamp:Date.now(),receivedAt:Date.now()};
 boats.set(p.sail,p);if(!tracks.has(p.sail))tracks.set(p.sail,[]);const t=tracks.get(p.sail);t.push([p.lat,p.lon]);if(t.length>1500)t.shift();
 const msg=JSON.stringify({type:"position",data:p});for(const c of wss.clients)if(c.readyState===WebSocket.OPEN)c.send(msg);res.json({ok:true});
});
wss.on("connection",ws=>ws.send(JSON.stringify({type:"snapshot",data:[...boats.values()],tracks:Object.fromEntries(tracks)})));
const PORT=process.env.PORT||3000;server.listen(PORT,"0.0.0.0",()=>console.log(`OptiLive server: http://0.0.0.0:${PORT}`));