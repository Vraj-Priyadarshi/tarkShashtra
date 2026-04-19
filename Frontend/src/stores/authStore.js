import { create } from "zustand";
import { persist } from "zustand/middleware";

const ROLE_PRIORITY = [
  "ACADEMIC_COORDINATOR",
  "FACULTY_MENTOR",
  "SUBJECT_TEACHER",
  "STUDENT",
];

const useAuthStore = create(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,

      get isAuthenticated() {
        return !!get().accessToken;
      },

      login: (authResponse) => {
        localStorage.setItem("accessToken", authResponse.accessToken);
        set({
          accessToken: authResponse.accessToken,
          _hasHydrated: true,
          user: {
            id: authResponse.userId,
            email: authResponse.email,
            roles: authResponse.roles,
            instituteId: authResponse.instituteId,
            instituteName: authResponse.instituteName,
            mustChangePassword: authResponse.mustChangePassword,
            isActive: true,
          },
        });
      },

      logout: () => {
        localStorage.removeItem("accessToken");
        set({ accessToken: null, user: null, activeView: "teacher" });
        window.location.href = "/login";
      },

      hasRole: (role) => {
        const user = get().user;
        return user?.roles?.includes(role) || false;
      },

      getPrimaryRole: () => {
        const user = get().user;
        if (!user?.roles) return null;
        for (const role of ROLE_PRIORITY) {
          if (user.roles.includes(role)) return role;
        }
        return null;
      },

      // For dual-role teachers (SUBJECT_TEACHER + FACULTY_MENTOR)
      activeView: "teacher",
      setActiveView: (view) => set({ activeView: view }),

      updateUser: (updates) => {
        set((state) => ({
          user: state.user ? { ...state.user, ...updates } : null,
        }));
      },

      updateToken: (newToken) => {
        localStorage.setItem("accessToken", newToken);
        set({ accessToken: newToken });
      },

      _hasHydrated: false,
      setHasHydrated: (val) => set({ _hasHydrated: val }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user,
        activeView: state.activeView,
      }),
      onRehydrateStorage: (state) => () => {
        // state is always the pre-rehydration store object (never null),
        // so setHasHydrated fires regardless of whether localStorage had data.
        state.setHasHydrated(true);
      },
    }
  )
);

export default useAuthStore;
