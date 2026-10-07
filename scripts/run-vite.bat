@echo off
set PATH=%PATH%;C:\Program Files\nodejs
cd /d C:\Users\Yash\Desktop\rec-platform\frontend
npm run dev -- --port 5173 > vite-run.log 2>&1
