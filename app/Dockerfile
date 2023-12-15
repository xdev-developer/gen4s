FROM openjdk:11.0.5-jre-slim-buster

LABEL MAINTAINER="Gen4s"

WORKDIR /opt/gen4s

ADD target/universal/gen4s-app-* ./
RUN mv gen4s-app-*/* ./
RUN rm -r gen4s-app-*/