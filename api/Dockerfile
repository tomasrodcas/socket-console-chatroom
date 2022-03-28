FROM openjdk:16
RUN mkdir /app
WORKDIR /app
COPY . .
RUN javac src/main/java/*.java
RUN java /app/src/main/java/Server
RUN java /app/src/main/java/Server