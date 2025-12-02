# Build stage
FROM node:lts as build-stage
ENV NPM_CONFIG_LOGLEVEL info
LABEL MAINTAINER="Natan Vieira Do Nascimento <natan@katan.org>"

WORKDIR /app
COPY package*.json ./
ENV NODE_OPTIONS "--max_old_space_size=4096"
RUN yarn ci
COPY . .
RUN yarn build-only

# Production stage
FROM nginx:stable-alpine as production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
COPY nginx.conf /etc/nginx/nginx.conf
COPY entrypoint.sh entrypoint.sh

# Git info. Must be set in --build-arg (see bin/build)
ARG GIT_BRANCH
ARG GIT_COMMIT
ENV VITE_GIT_COMMIT=${GIT_COMMIT}
ENV VITE_GIT_BRANCH=${GIT_BRANCH}

# Expose Nginx default ports
EXPOSE 80
EXPOSE 443

# Run appplication
RUN chmod +x ./entrypoint.sh
CMD ["./entrypoint.sh"]