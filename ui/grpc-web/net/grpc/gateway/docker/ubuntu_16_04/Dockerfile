# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM ubuntu:16.04

ARG MAKEFLAGS=-j8
ENV EMBED_OPENSSL false

RUN apt-get update && apt-get install -y \
  autoconf \
  automake \
  build-essential \
  curl \
  git \
  default-jdk \
  default-jre \
  libtool \
  libpcre3-dev \
  libssl-dev \
  make \
  zip

RUN git clone https://github.com/grpc/grpc-web /github/grpc-web

RUN cd /github/grpc-web && \
  ./scripts/init_submodules.sh

RUN cd /github/grpc-web/third_party/grpc && \
  make && make install

RUN cd /github/grpc-web && \
  make
