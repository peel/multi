# Pull base image
FROM ericvh/arm64-ubuntu

# Install dependencies
RUN apt-get update && apt-get install -y \
    git \
    make \
    gcc \
    openjdk-8-jre \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/hardkernel/wiringPi.git /tmp/wiringc1 &&\
    cd /tmp/wiringc1 &&\
    make install &&\
    rm -rf /tmp/wiringc1

# Define working directory
WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-armhf

ADD target/scala-2.11/led-notifier.jar /data/led-notifier.jar

# Define default command
CMD /usr/bin/java -jar /data/led-notifier.jar -DOtpConnection.trace=3 -p "mbox" -s "echo@127.0.0.1" -c "cookie" blink
