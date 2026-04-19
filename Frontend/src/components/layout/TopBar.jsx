import useUiStore from "../../stores/uiStore";

export default function TopBar() {
  const pageTitle = useUiStore((s) => s.pageTitle);

  return (
    <header className="h-16 bg-bg-secondary border-b border-border-light px-8 flex items-center sticky top-0 z-30">
      <h1 className="text-lg font-semibold text-text-primary">{pageTitle}</h1>
    </header>
  );
}
