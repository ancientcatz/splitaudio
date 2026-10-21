#!/system/bin/sh
# Start/stop AppRoute on the phone (runs as the shell user, e.g. via Shizuku or adb).
# Usage: sh /data/local/tmp/approute.sh start|stop|status
D=/data/local/tmp; LOG=$D/approute.log
# Music apps: audio from these stays on Bluetooth; routing is active only while one of them plays.
MUSIC="app.morphe.android.apps.youtube.music com.google.android.apps.youtube.music"
pid(){ pgrep -f "app_process /system/bin AppRoute" | head -1; }
case "$1" in
  start)
    [ -n "$(pid)" ] && { echo "already pid=$(pid)"; exit 0; }
    uid(){ pm list packages -U "$1" | grep "package:$1 " | sed 's/.*uid://'; }
    M=""; for p in $MUSIC; do u=$(uid "$p"); [ -n "$u" ] && M="$M${M:+,}$u"; done
    # Every app uid except the music apps (including work-profile / second-space uids); system uids (<10000) untouched.
    # User-installed apps (-3) first, so they are always inside the routing limit.
    U=$( { pm list packages -3 -U; pm list packages -U; } | sed 's/.*uid://' | tr ',' '\n' | awk -v m="$M" 'BEGIN{n=split(m,a,",");for(i=1;i<=n;i++)x[a[i]]=1} ($1%100000)>=10000 && !($1 in x) && !($1 in seen){seen[$1]=1;print}' | tr '\n' ' ')
    CLASSPATH=$D/approute.dex setsid nohup app_process /system/bin AppRoute "$M" $U >> $LOG 2>&1 < /dev/null &
    sleep 2; echo "started pid=$(pid)"; tail -2 $LOG ;;
  stop) P=$(pid); [ -n "$P" ] && kill $P; echo "stopped" ;;
  status) P=$(pid); echo "pid=${P:-none}"; tail -3 $LOG ;;
esac
