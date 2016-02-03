FROM clojure
MAINTAINER Andrew Rosa

RUN useradd -r -s /bin/false -m app
USER app
ADD project.clj /home/app/project.clj
ADD src /home/app/src
WORKDIR /home/app

RUN lein uberjar
WORKDIR /code

CMD \
  [ "java" \
  , "-XX:+UseParNewGC" \
  , "-XX:MinHeapFreeRatio=5" \
  , "-XX:MaxHeapFreeRatio=10" \
  , "-jar", "/home/app/target/codeclimate-kibit.jar", "." \
  , "-C", "/config.json" \
  ]
