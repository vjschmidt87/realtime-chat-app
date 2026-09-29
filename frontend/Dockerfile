# ---- Stage 1: Build ----
FROM node:20-alpine AS build

WORKDIR /app

# Copy dependency manifests first to leverage Docker layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the source code and build
COPY . .
RUN npm run build -- --configuration production

# ---- Stage 2: Serve with Nginx ----
FROM nginx:alpine

LABEL maintainer="realtime-chat-app"
LABEL description="Real-Time Chat Application - Angular Frontend"
LABEL version="1.0.0"

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built Angular app from the build stage
# Angular 17+ outputs to dist/<project-name>/browser/
COPY --from=build /app/dist/realtime-chat-frontend/browser/ /usr/share/nginx/html

# Set ownership to nginx user (provided by the nginx:alpine image)
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

USER nginx

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
