(function(root){
 function normalize(input){const out={};for(const [s,points] of Object.entries(input||{})){if(!Array.isArray(points))continue;const t=points.filter(p=>Number.isFinite(p.timestamp)&&Number.isFinite(p.lat)&&Number.isFinite(p.lon)&&Math.abs(p.lat)<=90&&Math.abs(p.lon)<=180).slice().sort((a,b)=>a.timestamp-b.timestamp);if(t.length)out[s]=t}return out}
 function position(t,time){let lo=0,hi=t.length;while(lo<hi){const m=(lo+hi)>>1;if(t[m].timestamp<=time)lo=m+1;else hi=m}if(!lo)return null;const a=t[lo-1],b=t[lo],f=b&&b.timestamp>a.timestamp?Math.max(0,Math.min(1,(time-a.timestamp)/(b.timestamp-a.timestamp))):0;return {lat:a.lat+(b?b.lat-a.lat:0)*f,lon:a.lon+(b?b.lon-a.lon:0)*f,index:lo}}
 function range(tracks,race){let min=Infinity,max=-Infinity;for(const t of Object.values(tracks)){if(t.length){min=Math.min(min,t[0].timestamp);max=Math.max(max,t[t.length-1].timestamp)}}if(!Number.isFinite(min))return null;const start=Number.isFinite(race.startedAt)?Math.min(race.startedAt,min):min,end=Number.isFinite(race.endedAt)?Math.max(race.endedAt,max):max;return {start,end}}
 function trail(t,time,windowMs=10000){
  if(!t.length)return [];const from=Math.max(t[0].timestamp,time-windowMs),to=Math.min(time,t[t.length-1].timestamp);if(to<=from)return [];
  const points=[];for(let i=0;i<=8;i++){const timestamp=from+(to-from)*i/8,p=position(t,timestamp);if(p)points.push({lat:p.lat,lon:p.lon,timestamp})}return points;
 }
 function heading(t,time){
  if(t.length<2)return null;const to=Math.min(time,t[t.length-1].timestamp),a=position(t,Math.max(t[0].timestamp,to-1000)),b=position(t,Math.min(t[t.length-1].timestamp,to+1000));
  if(!a||!b||Math.abs(a.lat-b.lat)+Math.abs(a.lon-b.lon)<1e-10)return null;
  const rad=Math.PI/180,la=a.lat*rad,lb=b.lat*rad,d=(b.lon-a.lon)*rad;return (Math.atan2(Math.sin(d)*Math.cos(lb),Math.cos(la)*Math.sin(lb)-Math.sin(la)*Math.cos(lb)*Math.cos(d))/rad+360)%360;
 }
 const engine={normalize,position,range,trail,heading};if(typeof module!=='undefined')module.exports=engine;else root.ReplayEngine=engine;
})(typeof globalThis!=='undefined'?globalThis:this);
