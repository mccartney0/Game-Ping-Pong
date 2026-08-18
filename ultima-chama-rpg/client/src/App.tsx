/**
 * Gravura de Cinzas — o app deixa a tela inteira para o canvas e para o HUD diegético do RPG.
 */
import ErrorBoundary from "./components/ErrorBoundary";
import GameCanvas from "./components/GameCanvas";

export default function App() {
  return <ErrorBoundary><GameCanvas /></ErrorBoundary>;
}
