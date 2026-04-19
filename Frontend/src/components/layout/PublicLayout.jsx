import { Outlet, Link, useLocation } from "react-router";
import { useState, useEffect } from "react";
import { cn } from "../../lib/utils";
import Button from "../ui/Button";

export default function PublicLayout() {
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();
  const isLanding = location.pathname === "/";

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <div className="min-h-screen bg-bg-primary">
      {/* Navbar */}
      <nav
        className={cn(
          "fixed top-0 left-0 right-0 z-50 transition-all duration-300 px-6",
          scrolled || !isLanding
            ? "bg-bg-secondary/90 backdrop-blur-md border-b border-border-light shadow-sm"
            : "bg-transparent"
        )}
      >
        <div className="max-w-7xl mx-auto h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-accent-primary flex items-center justify-center">
              <span className="text-white font-bold text-sm">T</span>
            </div>
            <span className="font-semibold text-text-primary text-lg">TarkShastra</span>
          </Link>

          <div className="flex items-center gap-6">
            <Link
              to="/about"
              className="text-sm text-text-secondary hover:text-text-primary transition-colors"
            >
              About
            </Link>
            <Link to="/login">
              <Button variant="dark" size="sm">
                Login
              </Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Page Content */}
      <main>
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="border-t border-border-light bg-bg-secondary">
        <div className="max-w-7xl mx-auto px-6 py-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-full bg-accent-primary flex items-center justify-center">
              <span className="text-white font-bold text-[10px]">T</span>
            </div>
            <span className="text-sm text-text-tertiary">
              © 2026 TarkShastra. All rights reserved.
            </span>
          </div>
          <div className="flex items-center gap-6 text-sm text-text-tertiary">
            <span className="hover:text-text-primary cursor-pointer transition-colors">
              Privacy
            </span>
            <span className="hover:text-text-primary cursor-pointer transition-colors">
              Terms
            </span>
            <span className="hover:text-text-primary cursor-pointer transition-colors">
              Contact
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
