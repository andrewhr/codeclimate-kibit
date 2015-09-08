FROM java
MAINTAINER Andrew Rosa

RUN useradd -r -s /bin/false app

WORKDIR /code
ADD target/codeclimate-kibit.jar /usr/src/app/codeclimate-kibit.jar

USER app

CMD ["java", "-jar", "/usr/src/app/codeclimate-kibit.jar", "/code", "-C", "/config.json"]
