import { NavLink } from "react-router";
import { cn } from "../../lib/utils";

export default function SidebarNavItem({ to, icon: Icon, label }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200",
          isActive
            ? "bg-white/10 text-white"
            : "text-white/60 hover:text-white hover:bg-white/5"
        )
      }
    >
      {Icon && <Icon className="w-5 h-5 flex-shrink-0" />}
      <span>{label}</span>
    </NavLink>
  );
}
