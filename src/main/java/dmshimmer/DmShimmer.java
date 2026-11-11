package dmshimmer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Plays a pre-baked loop of "Dark Matter" frames on the gun texture of dmcamo:deagle_dm.
 * TACZ gun textures are plain static textures, so we swap the pixels of that texture on the client.
 */
@Mod(value = DmShimmer.MODID, dist = Dist.CLIENT)
public class DmShimmer {
    public static final String MODID = "dmshimmer";

    /** Texture exactly as TACZ asks for it (display json: "dmcamo:gun/uv/deagle_dm"). */
    private static final ResourceLocation TARGET =
            ResourceLocation.fromNamespaceAndPath("dmcamo", "textures/gun/uv/deagle_dm.png");
    private static final String FRAME_FMT = "frames/deagle_dm_%02d.png";
    private static final int FRAMES = 16;
    /** Client ticks per frame: 2 = 10 fps (loop ~1.6 s). Raise it for a slower shimmer. */
    private static final int TICKS_PER_FRAME = 2;

    private final List<NativeImage> frames = new ArrayList<>();
    private DynamicTexture dyn;
    private int tick, frame = -1;

    public DmShimmer(IEventBus modBus) {
        modBus.addListener(this::onRegisterReload);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onRegisterReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) this::loadFrames);
    }

    private void loadFrames(ResourceManager rm) {
        frames.forEach(NativeImage::close);
        frames.clear();
        dyn = null;
        frame = -1;
        for (int i = 0; i < FRAMES; i++) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MODID, String.format(FRAME_FMT, i));
            try (InputStream in = rm.open(loc)) {
                frames.add(NativeImage.read(in));
            } catch (Exception e) {
                frames.forEach(NativeImage::close);
                frames.clear();
                return;
            }
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (frames.isEmpty() || mc.level == null || mc.isPaused()) return;
        if (++tick % TICKS_PER_FRAME != 0) return;

        TextureManager tm = mc.getTextureManager();
        // The gunpack may be absent or (re)loaded later than us: install our texture only when it exists,
        // and re-install if the texture manager replaced it after a resource reload.
        if (dyn == null || tm.getTexture(TARGET) != dyn) {
            if (mc.getResourceManager().getResource(TARGET).isEmpty()) return;
            NativeImage first = frames.get(0);
            NativeImage img = new NativeImage(first.getWidth(), first.getHeight(), false);
            img.copyFrom(first);
            dyn = new DynamicTexture(img);
            dyn.setFilter(true, false);
            tm.register(TARGET, dyn);
            frame = 0;
            return;
        }
        frame = (frame + 1) % frames.size();
        NativeImage pixels = dyn.getPixels();
        if (pixels == null) return;
        pixels.copyFrom(frames.get(frame));
        dyn.upload();
    }
}
