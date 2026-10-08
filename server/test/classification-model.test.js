const {test}=require('node:test'),assert=require('node:assert/strict');
const {rows}=require('../public/classification-model');
test('selected race rows filter actual profiles, pick latest fix and never invent ranks',()=>{
 const tracks={'ESP1606':[{timestamp:2,lat:39,lon:4,name:'Borja',category:'Sub11',club:'Mahón'},{timestamp:1,lat:39,lon:4,name:'Anterior',category:'Sub13'}],ESP200:[{timestamp:3,lat:39,lon:4}]};
 assert.equal(rows(tracks,{category:'Sub11'})[0].name,'Borja');assert.equal(rows(tracks,{category:'Sub13'}).length,0);
 assert.equal(rows(tracks,{search:'borja'}).length,1);assert.equal(rows(tracks)[0].sail,'ESP200');
 assert.equal(rows(tracks).find(p=>p.sail==='ESP200').category,'');assert.equal(rows(tracks).some(p=>p.rank!==undefined),false);
 assert.equal(rows({}).length,0);
});
test('demo metadata stays marked synthetic and real archives do not acquire demo names',()=>{
 const tracks={'ESP-DEMO01':[{timestamp:1,lat:39,lon:4}]};assert.equal(rows(tracks,{demo:true,category:'Sub11'}).length,1);
 assert.equal(rows(tracks)[0].name,'Nombre no registrado');
});
