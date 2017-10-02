java \
  -Duser.language=en \
  -Duser.country=US \
  -Dfile.encoding=UTF-8 \
  -jar ${project.build.finalName}.jar \
  --max-streets 2 \
  --max-buildings 3 \
  --max-houses 3 \
  --max-consumers 2
