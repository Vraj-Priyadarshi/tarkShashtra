import { create } from "zustand";

const useUiStore = create((set) => ({
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setSidebarOpen: (open) => set({ sidebarOpen: open }),

  pageTitle: "Dashboard",
  setPageTitle: (title) => set({ pageTitle: title }),
}));

export default useUiStore;
