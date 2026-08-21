package com.mccartney0.gamepingpong;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public final class MobileMenu extends InputAdapter {
    public enum Page {
        MAIN,
        MODES,
        HELP,
        SETTINGS,
        PAUSE,
        RESULTS
    }

    public interface Listener {
        void onStartMode(MobileGameMode mode);
        void onResumeGame();
        void onOpenMainMenu();
        void onOpenHelp();
        void onOpenSettings();
        void onShowLeaderboards();
        void onShowAchievements();
        void onShowRewarded();
        void onCycleEffectsQuality();
    }

    private final Listener listener;
    private Page page = Page.MAIN;
    private int selection;
    private int screenHeight = 600;
    private MobileGameMode selectedMode = MobileGameMode.CLASSIC;

    public MobileMenu(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener obrigatorio");
        }
        this.listener = listener;
    }

    public void setScreenSize(int height) {
        screenHeight = Math.max(1, height);
    }

    public Page getPage() {
        return page;
    }

    public int getSelection() {
        return selection;
    }

    public MobileGameMode getSelectedMode() {
        return selectedMode;
    }

    public void showMain() {
        page = Page.MAIN;
        selection = 0;
    }

    public void showModes() {
        page = Page.MODES;
        selection = selectedMode.ordinal();
    }

    public void showHelp() {
        page = Page.HELP;
        selection = 0;
    }

    public void showSettings() {
        page = Page.SETTINGS;
        selection = 0;
    }

    public void showPause() {
        page = Page.PAUSE;
        selection = 0;
    }

    public void showResults() {
        page = Page.RESULTS;
        selection = 0;
    }

    public int getItemCount() {
        switch (page) {
        case MODES:
            return MobileGameMode.values().length + 1;
        case SETTINGS:
            return 2;
        case PAUSE:
        case RESULTS:
            return 2;
        case HELP:
            return 1;
        case MAIN:
        default:
            return 7;
        }
    }

    public String getItemLabel(int index) {
        switch (page) {
        case MODES:
            if (index < MobileGameMode.values().length) {
                return MobileGameMode.values()[index].getLabel();
            }
            return "VOLTAR";
        case SETTINGS:
            return index == 0 ? "QUALIDADE DOS EFEITOS" : "VOLTAR";
        case PAUSE:
            return index == 0 ? "CONTINUAR" : "VOLTAR AO MENU";
        case RESULTS:
            return index == 0 ? "JOGAR NOVAMENTE" : "VOLTAR AO MENU";
        case HELP:
            return "VOLTAR";
        case MAIN:
        default:
            switch (index) {
            case 0: return "JOGAR";
            case 1: return "ESCOLHER MODO";
            case 2: return "COMO JOGAR";
            case 3: return "CONFIGURACOES";
            case 4: return "PLACARES";
            case 5: return "CONQUISTAS";
            case 6: return "VER RECOMPENSA";
            default: return "";
            }
        }
    }

    public boolean tapRow(int row) {
        if (row < 0 || row >= getItemCount()) {
            return false;
        }
        selection = row;
        activateSelection();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer < 0) {
            return false;
        }
        int header = Math.max(72, screenHeight / 6);
        int rowHeight = Math.max(48, screenHeight / 9);
        int row = (screenY - header) / rowHeight;
        return tapRow(row);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.UP) {
            selection = (selection + getItemCount() - 1) % getItemCount();
            return true;
        }
        if (keycode == Input.Keys.DOWN) {
            selection = (selection + 1) % getItemCount();
            return true;
        }
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
            activateSelection();
            return true;
        }
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            goBack();
            return true;
        }
        return false;
    }

    private void activateSelection() {
        switch (page) {
        case MAIN:
            activateMainSelection();
            break;
        case MODES:
            if (selection < MobileGameMode.values().length) {
                selectedMode = MobileGameMode.values()[selection];
                listener.onStartMode(selectedMode);
            } else {
                showMain();
            }
            break;
        case SETTINGS:
            if (selection == 0) {
                listener.onCycleEffectsQuality();
            } else {
                showMain();
            }
            break;
        case PAUSE:
            if (selection == 0) {
                listener.onResumeGame();
            } else {
                listener.onOpenMainMenu();
                showMain();
            }
            break;
        case RESULTS:
            if (selection == 0) {
                listener.onStartMode(selectedMode);
            } else {
                listener.onOpenMainMenu();
                showMain();
            }
            break;
        case HELP:
        default:
            listener.onOpenMainMenu();
            showMain();
            break;
        }
    }

    private void activateMainSelection() {
        switch (selection) {
        case 0:
            listener.onStartMode(selectedMode);
            break;
        case 1:
            showModes();
            break;
        case 2:
            listener.onOpenHelp();
            showHelp();
            break;
        case 3:
            listener.onOpenSettings();
            showSettings();
            break;
        case 4:
            listener.onShowLeaderboards();
            break;
        case 5:
            listener.onShowAchievements();
            break;
        case 6:
            listener.onShowRewarded();
            break;
        default:
            break;
        }
    }

    private void goBack() {
        if (page == Page.MAIN) {
            return;
        }
        if (page == Page.PAUSE) {
            listener.onResumeGame();
        } else {
            listener.onOpenMainMenu();
            showMain();
        }
    }
}
