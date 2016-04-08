FROM peelsky/odroid-c2-archlinux

RUN pacman -Syyu --noconfirm jre8-openjdk wiringc1
ADD target/scala-2.11/led-notifier.jar /data/led-notifier.jar

ENTRYPOINT /usr/bin/java -jar /data/led-notifier.jar -DOtpConnection.trace=3
CMD -p "mbox" -s "echo@127.0.0.1" -c "cookie" blink
