(function(root){
const R=6371000,rad=Math.PI/180;
function xy(p,o){return [(p.lon-o.lon)*rad*R*Math.cos(o.lat*rad),(p.lat-o.lat)*rad*R]}
function length(a,b){const [x,y]=xy(b,a);return Math.hypot(x,y)}
// Only the explicitly known demo course supports GPS ranking. Real races require a configured ordered course.
function course(marks){const m=n=>marks.find(p=>p.name===n);const s=m('S1'),s2=m('S2'),l=m('L1'),l2=m('L2');if(!s||!s2||!l||!l2||![m('1'),m('2'),m('3')].every(Boolean))return null;return [{lat:(s.lat+s2.lat)/2,lon:(s.lon+s2.lon)/2},m('1'),m('2'),m('3'),{lat:(l.lat+l2.lat)/2,lon:(l.lon+l2.lon)/2}]}
function progress(track,time,route){let leg=0,finish=null;for(const p of track){if(p.timestamp>time)break;if(leg<route.length-1&&length(p,route[leg+1])<=(leg===route.length-2?5:60)){leg++;if(leg===route.length-1)finish=p.timestamp}}
 if(finish!==null)return {score:Infinity,finish};const p=root.ReplayEngine.position(track,time);if(!p)return null;const a=route[leg],b=route[leg+1],v=xy(b,a),w=xy(p,a),len=Math.hypot(...v);const fraction=Math.max(0,Math.min(.999,(v[0]*w[0]+v[1]*w[1])/(len*len)));return {score:leg+fraction,finish:null}}
function rows(input,time,{demo=false,marks=[]}={}){const tracks=root.ReplayEngine.normalize(input),route=demo?course(marks):null;const list=[];for(const [sail,t] of Object.entries(tracks)){const p=root.ReplayEngine.position(t,time);if(!p)continue;const i=Math.min(t.length-1,p.index-1),a=t[i],b=t[i+1],f=b?Math.max(0,Math.min(1,(time-a.timestamp)/(b.timestamp-a.timestamp))):0;const speed=Number.isFinite(a.speedKnots)?a.speedKnots+(b&&Number.isFinite(b.speedKnots)?b.speedKnots-a.speedKnots:0)*f:null;const n=Number(sail.match(/DEMO(\d+)$/)?.[1]);list.push({...a,sail,name:a.name||(demo?'Regatista demo '+n:'Nombre no registrado'),category:a.category||(demo?(n<=2?'Sub11':n<=4?'Sub13':'Sub15'):''),speedKnots:speed,heading:root.ReplayEngine.heading(t,time),rank:null,progress:route?progress(t,time,route):null})}
 list.sort((a,b)=>a.progress&&b.progress?((a.progress.finish!==null&&b.progress.finish!==null)?a.progress.finish-b.progress.finish:b.progress.score-a.progress.score)||a.sail.localeCompare(b.sail):a.sail.localeCompare(b.sail));
 if(route)list.forEach((p,i)=>{p.rank=i&&p.progress.score===list[i-1].progress.score&&p.progress.finish===list[i-1].progress.finish?list[i-1].rank:i+1});return list}
const api={rows,course,progress};root.FleetModel=api;if(typeof module!=='undefined')module.exports=api;
})(typeof globalThis!=='undefined'?globalThis:this);
