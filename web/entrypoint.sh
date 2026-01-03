#!/bin/sh

join_by() {
	local IFS="$1";
	shift;
	echo "$*";
}

# Find Vue environment vars
vars=$(env | grep VITE_ | awk -F = '{print "$"$1}')
vars=$(join_by ',' "$vars")
echo "Replacing Vue environment variables: $vars"

for file in /app/assets/*.js;
do
  echo "Processing $file...";

  # Use the existing JS file as template
  cp "$file" "$file".tmpl
  envsubst "$vars" < "$file".tmpl > "$file"
  rm "$file".tmpl
done

echo "Küken UI is ready.";
nginx -g 'daemon off;'