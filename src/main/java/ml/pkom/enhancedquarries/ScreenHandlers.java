package ml.pkom.enhancedquarries;

import ml.pkom.enhancedquarries.screen.*;
import ml.pkom.mcpitanlibarch.api.gui.SimpleScreenHandlerTypeBuilder;
import net.minecraft.screen.ScreenHandlerType;

import static ml.pkom.enhancedquarries.EnhancedQuarries.registry;

public class ScreenHandlers {
    public static ScreenHandlerType<FillerScreenHandler> FILLER_SCREEN_HANDLER_TYPE = new SimpleScreenHandlerTypeBuilder<>(FillerScreenHandler::new).build();
    public static ScreenHandlerType<FillerWithChestScreenHandler> FILLER_WITH_CHEST_SCREEN_HANDLER_TYPE = new SimpleScreenHandlerTypeBuilder<>(FillerWithChestScreenHandler::new).build();
    public static ScreenHandlerType<ScannerScreenHandler> SCANNER_SCREEN_HANDLER_TYPE = new SimpleScreenHandlerTypeBuilder<>(ScannerScreenHandler::new).build();
    public static ScreenHandlerType<BuilderScreenHandler> BUILDER_SCREEN_HANDLER_TYPE = new SimpleScreenHandlerTypeBuilder<>(BuilderScreenHandler::new).build();
    public static ScreenHandlerType<LibraryScreenHandler> LIBRARY_SCREEN_HANDLER_TYPE = new SimpleScreenHandlerTypeBuilder<>(LibraryScreenHandler::new).build();


    public static void init() {
        registry.registerScreenHandlerType(EnhancedQuarries.id("filler"), () -> FILLER_SCREEN_HANDLER_TYPE);
        registry.registerScreenHandlerType(EnhancedQuarries.id("filler_with_chest"), () -> FILLER_WITH_CHEST_SCREEN_HANDLER_TYPE);
        registry.registerScreenHandlerType(EnhancedQuarries.id("scanner"), () -> SCANNER_SCREEN_HANDLER_TYPE);
        registry.registerScreenHandlerType(EnhancedQuarries.id("builder"), () -> BUILDER_SCREEN_HANDLER_TYPE);
        registry.registerScreenHandlerType(EnhancedQuarries.id("library"), () -> LIBRARY_SCREEN_HANDLER_TYPE);
    }
}
