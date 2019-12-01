#
# Baixa o último Release de CDR2ORC para uma imagem.
#
#
# Criar a imagem:
#
#   docker build --tag cdr2orc .
#
# Rodar:
#
#   docker run cdr2orc
#
# Pegar o IP:
#
#   docker inspect <nome_do_container_rodando> | grep Address
#
# Copiar arquivos CDR para o container:
#
#   docker cp <arquivo_cdr.txt> <nome_do_container>:/root/Spring_CDR2ORC/Release/data/CDR
#
#   (A aplicação irá converter o CDR automaticamente, e disponibilizá-lo para consultas
#    a partir da interface WEB)

FROM ubuntu:18.04
MAINTAINER Vander

RUN ln -fs /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime
RUN apt-get update
RUN apt-get install -y \
#  cmake \
#  gcc \
#  g++ \
  git \
  libsasl2-dev \
  libssl-dev \
#  make \
#  maven \
  openjdk-8-jdk \
  tzdata
RUN update-java-alternatives --set java-1.8.0-openjdk-amd64
RUN git clone https://github.com/imerso/Spring_CDR2ORC -b master /root/Spring_CDR2ORC

WORKDIR /root
EXPOSE 8080/tcp
CMD cd /root/Spring_CDR2ORC/Release && java -jar CDR2ORC.jar

