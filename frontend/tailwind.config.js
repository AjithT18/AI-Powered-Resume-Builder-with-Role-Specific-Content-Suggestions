/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
      colors: {
        ink: "#1f2933",
        steel: "#52616b",
        mint: "#2f855a",
        ember: "#c2410c",
        paper: "#f8faf9",
      },
      boxShadow: {
        panel: "0 14px 40px rgba(31, 41, 51, 0.08)",
      },
    },
  },
  plugins: [],
};
