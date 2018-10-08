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

FROM ubuntu:12.04
RUN apt-get update && apt-get install -y \
  autoconf \
  automake \
  build-essential \
  curl \
  git \
  libtool \
  make \
  python-software-properties \
  software-properties-common \
  zip
RUN add-apt-repository ppa:ubuntu-toolchain-r/test
RUN apt-get update && apt-get install -y \
  g++-4.7 \
  gcc-4.7 \
  libpcre3 \
  libpcre3-dev \
  libssl-dev
RUN update-alternatives \
  --remove gcc /usr/bin/gcc-4.6
RUN update-alternatives \
  --install /usr/bin/gcc gcc /usr/bin/gcc-4.7 60 \
  --slave /usr/bin/g++ g++ /usr/bin/g++-4.7
RUN update-alternatives \
  --install /usr/bin/gcc gcc /usr/bin/gcc-4.6 40 \
  --slave /usr/bin/g++ g++ /usr/bin/g++-4.6
RUN git clone https://github.com/grpc/grpc-web /github/grpc-web
RUN cd /github/grpc-web/third_party/grpc && \
  make
RUN cd /github/grpc-web && \
  make
