import { FileText, LogOut, Menu, UserRound } from "lucide-react";
import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function AppNavbar() {
  const [open, setOpen] = useState(false);
  const { token, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/");
  }

  const links = token
    ? [
        { label: "Dashboard", to: "/dashboard" },
        { label: "Builder", to: "/builder" },
      ]
    : [
        { label: "Login", to: "/login" },
        { label: "Register", to: "/register" },
      ];

  return (
    <header className="border-b border-slate-200 bg-white/95">
      <div className="container-pad flex h-16 items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-2 text-lg font-black text-ink">
          <FileText className="h-6 w-6 text-mint" />
          EliteResume
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `rounded-md px-3 py-2 text-sm font-semibold ${isActive ? "bg-emerald-50 text-mint" : "text-steel hover:bg-slate-100"}`
              }
            >
              {link.label}
            </NavLink>
          ))}
          {token && (
            <div className="ml-2 flex items-center gap-2 border-l border-slate-200 pl-3">
              <span className="inline-flex max-w-48 items-center gap-2 truncate text-sm text-steel">
                <UserRound className="h-4 w-4" />
                {user?.email}
              </span>
              <button className="icon-btn" type="button" onClick={handleLogout} title="Logout">
                <LogOut className="h-4 w-4" />
              </button>
            </div>
          )}
        </nav>

        <button className="icon-btn md:hidden" type="button" onClick={() => setOpen((value) => !value)} title="Menu">
          <Menu className="h-5 w-5" />
        </button>
      </div>

      {open && (
        <div className="border-t border-slate-200 bg-white px-4 py-3 md:hidden">
          <div className="flex flex-col gap-2">
            {links.map((link) => (
              <Link key={link.to} to={link.to} className="rounded-md px-3 py-2 text-sm font-semibold text-ink" onClick={() => setOpen(false)}>
                {link.label}
              </Link>
            ))}
            {token && (
              <button className="btn-secondary justify-start" type="button" onClick={handleLogout}>
                <LogOut className="h-4 w-4" />
                Logout
              </button>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
