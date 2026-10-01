const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const path = require("path");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });
const boats = new Map();

app.use(express.json({ limit: "256kb" }));
app.use(express.static(path.join(__dirname, "public")));

app.get("/api/health", (_req, res) => res.json({ ok: true, boats: boats.size, time: Date.now() }));
app.get("/api/boats", (_req, res) => res.json([...boats.values()]));

app.post("/api/position", (req, res) => {
  const { sail, lat, lon, accuracy, timestamp } = req.body || {};
  if (!sail || !Number.isFinite(lat) || !Number.isFinite(lon)) {
    return res.status(400).json({ ok: false, error: "sail, lat and lon required" });
  }
  const position = {
    sail: String(sail).trim(),
    lat, lon,
    accuracy: Number.isFinite(accuracy) ? accuracy : null,
    timestamp: Number.isFinite(timestamp) ? timestamp : Date.now(),
    receivedAt: Date.now()
  };
  boats.set(position.sail, position);
  const message = JSON.stringify({ type: "position", data: position });
  for (const client of wss.clients) if (client.readyState === WebSocket.OPEN) client.send(message);
  res.json({ ok: true });
});

wss.on("connection", ws => {
  ws.send(JSON.stringify({ type: "snapshot", data: [...boats.values()] }));
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, "0.0.0.0", () => console.log(`OptiLive server: http://0.0.0.0:${PORT}`));
