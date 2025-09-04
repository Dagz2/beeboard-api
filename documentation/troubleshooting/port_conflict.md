# Port 8080 Troubleshooting Guide

Quick reference for checking and killing processes using port 8080 (common Quarkus dev port).

## 🔍 Find Process Using Port 8080

### Windows
```cmd
# Check what's using port 8080
netstat -ano | findstr :8080

# Alternative with PowerShell
Get-NetTCPConnection -LocalPort 8080
```

### macOS
```bash
# Check what's using port 8080
lsof -i :8080

# Alternative
netstat -an | grep 8080
```

### Linux
```bash
# Check what's using port 8080
lsof -i :8080

# Alternative
netstat -tlnp | grep :8080

# Or with ss command
ss -tlnp | grep :8080
```

## 💀 Kill Process by PID

### Windows
```cmd
# Kill by PID (replace XXXX with actual PID)
taskkill /PID XXXX /F

# Kill all Java processes (use with caution!)
taskkill /IM java.exe /F
```

### macOS & Linux
```bash
# Kill by PID (replace XXXX with actual PID)
kill -9 XXXX

# Kill all Java processes (use with caution!)
pkill -f java
```

## 🚀 One-liner Solutions

### Windows (PowerShell)
```powershell
# Find and kill in one command
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess | ForEach-Object {taskkill /PID $_.OwningProcess /F}
```

### macOS & Linux
```bash
# Find and kill in one command
lsof -ti:8080 | xargs kill -9
```

## ⚠️ Common Quarkus Scenarios

- **Dev mode stuck**: Usually happens when stopping Quarkus dev mode improperly
- **Port already in use**: Another Quarkus instance or different service is running
- **IDE issues**: Sometimes IDEs don't release the port properly

## 💡 Prevention Tips

- Always use `Ctrl+C` to stop Quarkus dev mode properly
- Check for zombie Java processes after unexpected shutdowns
- Use `./mvnw quarkus:dev -Ddebug=false` to avoid debug port conflicts