FROM openjdk:17-jdk

# Instala dependencias necesarias
RUN apt-get update && \
    apt-get install -y wget unzip

# Instala Android SDK Command Line Tools
ENV ANDROID_SDK_ROOT /sdk
RUN mkdir -p $ANDROID_SDK_ROOT
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip && \
    unzip cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools && \
    mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest && \
    rm cmdline-tools.zip

ENV PATH $PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
ENV PATH $PATH:$ANDROID_SDK_ROOT/platform-tools

# Instala las plataformas y build-tools necesarios
RUN yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses
RUN sdkmanager --sdk_root=$ANDROID_SDK_ROOT "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Copia el proyecto al contenedor
WORKDIR /app
COPY . /app

# Compila el proyecto
CMD ["./gradlew", "assembleDebug"]