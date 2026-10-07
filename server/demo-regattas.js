// Synthetic archive fixtures. Never insert into live boats, positions or websocket feeds.
function seedDemoRegattas({regattas,races,raceTracks}){
 const examples=[{id:'DEMO-MAHON',name:'DEMO · Regata de Mahón',club:'Club ficticio · Mahón',venue:'Puerto de Mahón · datos simulados',lat:39.871,lon:4.303,boats:6},{id:'DEMO-CIUTADELLA',name:'DEMO · Regata de Ciutadella',club:'Club ficticio · Ciutadella',venue:'Costa de Ciutadella · datos simulados',lat:39.990,lon:3.820,boats:4}];let created=0;
 for(const g of examples){if(regattas.has(g.id))continue;created++;const first=Date.parse('2026-10-05T10:00:00+02:00');regattas.set(g.id,{id:g.id,name:g.name,club:g.club,venue:g.venue,startDate:'2026-10-05',endDate:'2026-10-06',days:[{day:1,maxRaces:2},{day:2,maxRaces:1}],status:'finished',demo:true,createdAt:first,description:'Demostración: barcos y posiciones completamente ficticios.'});
 for(let number=1;number<=3;number++){
 const day=number===3?2:1,date=day===1?'2026-10-05':'2026-10-06',startedAt=first+(day-1)*86400000+(number===2?3600000:0),duration=(12+number*2)*60000,endedAt=startedAt+duration;
 const offset=(number-1)*.0003,lat=g.lat+offset,lon=g.lon-offset;
 const coords=[['S1','Salida babor',lat,lon-.001],['S2','Salida estribor',lat,lon+.001],['1','Barlovento',lat+.008,lon+.002],['2','Boya 2',lat+.004,lon-.006],['3','Sotavento',lat-.001,lon-.002],['L1','Llegada babor',lat+.0005,lon-.0006],['L2','Llegada estribor',lat+.0005,lon+.0006]];
 const marks=coords.map(([name,type,la,lo],i)=>({regattaId:g.id,deviceId:g.id+'-R'+number+'-M'+i,name,type,lat:la,lon:lo,positionSource:'demo',demo:true}));
 const key=g.id+':'+day+':'+number;races.set(key,{regattaId:g.id,day,number,date,plannedTime:'10:00',status:'Finalizada',startedAt,endedAt,updatedAt:endedAt,marks,demo:true});
 const tracks={};for(let boat=0;boat<g.boats;boat++){
 const sail='ESP-DEMO'+String(boat+1).padStart(2,'0'),finishFraction=.87+boat*.023,boatDuration=duration*finishFraction;
 const route=[[lat,lon-.00065+boat*.00024],[lat+.008,lon+.002],[lat+.004,lon-.006],[lat-.001,lon-.002],[lat+.0005,lon]];
 const pts=[];for(let elapsed=0;elapsed<=duration;elapsed+=5000){const progress=Math.min(1,elapsed/boatDuration),segment=Math.min(3,Math.floor(progress*4)),f=progress>=1?1:progress*4-segment,a=route[segment],b=route[segment+1];const spread=Math.sin(Math.PI*f)*(.00012+boat*.00003),tack=segment===0?Math.sin(f*Math.PI*8+boat*.6)*.00023*Math.sin(Math.PI*f):0;
 pts.push({sail,lat:a[0]+(b[0]-a[0])*f+spread,lon:a[1]+(b[1]-a[1])*f+tack+spread*(boat%2?1:-1),timestamp:startedAt+elapsed,receivedAt:endedAt,accuracy:5,speedKnots:progress>=1?0:3.4+Math.sin(elapsed/22000+boat)*.8,regattaId:g.id,day,number,demo:true})}
 tracks[sail]=pts;
 }raceTracks.set(key,tracks);
 }
 }return created;
}
module.exports={seedDemoRegattas};
