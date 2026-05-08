import { Contact, Home, Info, UserRound } from "lucide-react";
import { NavLink } from "react-router-dom";
import AppNavbar from "./AppNavbar.jsx";

const items = [
  { label: "Profile", icon: UserRound, to: "/dashboard" },
  { label: "Home", icon: Home, to: "/" },
  { label: "About Us", icon: Info, to: "/dashboard#about" },
  { label: "Contact", icon: Contact, to: "/dashboard#contact" },
];

export default function DashboardLayout({ children }) {
  return (
    <div className="page-shell bg-paper/80">
      <AppNavbar />
      <div className="container-pad grid gap-6 py-6 lg:grid-cols-[240px_1fr]">
        <aside className="panel h-max p-3">
          <nav className="grid gap-1">
            {items.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.label}
                  to={item.to}
                  className="flex h-11 items-center gap-3 rounded-md px-3 text-sm font-semibold text-steel hover:bg-slate-100"
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </NavLink>
              );
            })}
          </nav>
        </aside>
        <main>{children}</main>
      </div>
    </div>
  );
}
