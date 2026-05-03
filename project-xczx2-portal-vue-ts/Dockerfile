# ===== Stage 1: build =====
FROM node:16-alpine AS builder
WORKDIR /app

RUN npm config set registry https://registry.npmmirror.com

COPY package.json ./
RUN npm install --legacy-peer-deps --registry=https://registry.npmmirror.com

COPY . .
RUN npm run build-prod

# ===== Stage 2: runtime =====
FROM nginx:alpine
LABEL maintainer="xuecheng-plus portal"
COPY --from=builder /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
