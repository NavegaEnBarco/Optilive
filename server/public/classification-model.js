(function(root){
 function rows(tracks,{category='',search='',demo=false,club=''}={}){
  const query=search.toLocaleLowerCase('es').trim();
  return Object.entries(tracks||{}).map(([sail,track])=>{
   const valid=(Array.isArray(track)?track:[]).filter(p=>Number.isFinite(p.timestamp)&&Number.isFinite(p.lat)&&Number.isFinite(p.lon));
   if(!valid.length)return null;
   const p=valid.reduce((a,b)=>b.timestamp>a.timestamp?b:a);
   const number=Number(sail.match(/DEMO(\d+)$/)?.[1]);
   const name=p.name||(demo?'Regatista demo '+(number||''):'Nombre no registrado');
   // Demo presentation metadata remains explicitly fictitious and separate from real profiles.
   const cat=p.category||(demo?(number<=2?'Sub11':number<=4?'Sub13':'Sub15'):'');
   return {...p,sail,name,category:cat,club:p.club||(demo?club:'Club no registrado')};
  }).filter(p=>p&&(!category||p.category===category)&&(!query||(p.sail+' '+p.name).toLocaleLowerCase('es').includes(query)))
  .sort((a,b)=>a.sail.localeCompare(b.sail,'es',{numeric:true}));
 }
 const api={rows};root.ClassificationModel=api;if(typeof module!=='undefined')module.exports=api;
})(typeof globalThis!=='undefined'?globalThis:this);
