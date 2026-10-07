const {test}=require('node:test'),assert=require('node:assert/strict'),{spawn}=require('node:child_process'),fs=require('node:fs'),os=require('node:os'),path=require('node:path');
test('schedule and independent courses persist; active and finished courses are protected',async()=>{
 const dir=fs.mkdtempSync(path.join(os.tmpdir(),'opti-agenda-'));let child;const base='http://127.0.0.1:31880';let token;
 async function boot(){child=spawn(process.execPath,['server.js'],{cwd:path.join(__dirname,'..'),env:{...process.env,PORT:'31880',OPTILIVE_DATA_FILE:path.join(dir,'data.json'),OPTILIVE_ADMIN_PASSWORD:'test-password'}});await new Promise((resolve,reject)=>{child.stdout.on('data',d=>{if(String(d).includes('server ready'))resolve()});child.once('exit',()=>reject(Error('Server failed')));child.stderr.on('data',d=>process.stderr.write(d))})}
 async function stop(){await new Promise(r=>{child.once('exit',r);child.kill()})}
 async function call(url,body,status=200,method){const r=await fetch(base+url,{method:method||(body?'POST':'GET'),headers:{'Content-Type':'application/json',Authorization:'Bearer '+token},body:body?JSON.stringify(body):undefined});assert.equal(r.status,status,await r.clone().text());return r.json()}
 try{await boot();token=(await call('/api/auth/login',{username:'admin',password:'test-password'})).token;const g=await call('/api/admin/regattas',{name:'Agenda'},201),admin='/api/admin/regattas/'+g.id,publicUrl='/api/regattas/'+g.id;
 await call(admin+'/schedule',{days:[{date:'2026-10-10',count:3},{date:'2026-10-11',count:3}]},201);const all=await call(publicUrl+'/races');assert.deepEqual(all.map(r=>r.number),[1,2,3,4,5,6]);assert.equal(all[3].day,2);
 await call(admin+'/schedule',{days:[{date:'2026-10-12',count:1}]},409);
 const mark=await call(admin+'/races/1/1/marks',{name:'B1',lat:39,lon:4},201);await call(admin+'/races/1/2/copy-course',{day:1,number:1});await call(admin+'/races/1/2/marks/'+mark.deviceId,{lat:40,lon:5},200,'PUT');assert.equal((await call(publicUrl+'/races/1/1/marks'))[0].lat,39);
 await call(admin+'/races',{day:1,number:1,status:'Preparada'},201);await call(admin+'/races',{day:1,number:1,status:'En curso'},201);await call(admin+'/races/1/1/marks/'+mark.deviceId,{lat:42,lon:5},409,'PUT');await call(admin+'/races',{day:1,number:2,status:'En curso'},409);await call(admin+'/races',{day:1,number:1,status:'Finalizada'},201);await call(admin+'/races',{day:1,number:1,status:'Pendiente'},409);
 await stop();await boot();assert.equal((await call(publicUrl+'/races')).length,6);assert.equal((await call(publicUrl+'/races/1/2/marks'))[0].lat,40);
 }finally{if(child&&!child.killed)await stop();fs.rmSync(dir,{recursive:true,force:true})}
});
