// One flag per finish endpoint; the foot of the pole anchors to its GPS position.
(function(root){
 const isFinish=m=>/^llegada(?:\s|$)/i.test(String(m.type||'').trim())||/^L\d*$/i.test(String(m.name||'').trim());
 let checks='';for(let row=0;row<4;row++)for(let col=0;col<6;col++)if((row+col)%2===0)checks+='<rect x="'+(7+col*4)+'" y="'+(4+row*4)+'" width="4" height="4" fill="#111"/>';
 const html='<svg width="36" height="42" viewBox="0 0 36 42" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bandera de llegada" style="filter:drop-shadow(0 1px 2px #fff)"><path d="M5 3v36" stroke="white" stroke-width="5"/><path d="M5 3v36" stroke="#162c3b" stroke-width="2"/><rect x="7" y="4" width="24" height="16" fill="white"/>'+checks+'<rect x="7" y="4" width="24" height="16" fill="none" stroke="#162c3b" stroke-width="1"/></svg>';
 root.FinishMarkers={isFinish,icon:()=>L.divIcon({className:'',html,iconSize:[36,42],iconAnchor:[5,39],tooltipAnchor:[13,-24]})};
})(globalThis);
