import AppNavbar from "../components/AppNavbar.jsx";
import AuthForm from "../components/AuthForm.jsx";

export default function RegisterPage() {
  return (
    <div className="page-shell">
      <AppNavbar />
      <main className="container-pad flex min-h-[calc(100vh-4rem)] items-center justify-center py-10">
        <AuthForm mode="register" />
      </main>
    </div>
  );
}
