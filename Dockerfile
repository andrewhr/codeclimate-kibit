FROM clojure
MAINTAINER Andrew Rosa

RUN useradd -r -s /bin/false -m app
USER app
ADD . /home/app
WORKDIR /home/app

RUN lein uberjar
WORKDIR /code

CMD ["java", "-jar", "/home/app/target/codeclimate-kibit.jar", ".", "-C", "/config.json"]
