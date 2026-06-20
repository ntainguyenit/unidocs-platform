/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1e3a8a',
          dark: '#0f172a'
        },
        text: {
          DEFAULT: '#111827'
        },
        divider: '#e5e7eb'
      }
    },
  },
  plugins: [],
}
