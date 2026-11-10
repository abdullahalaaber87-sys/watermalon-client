import {
  Aperture,
  Box,
  Boxes,
  Camera,
  CloudSun,
  Crosshair,
  Dice5,
  Eye,
  Footprints,
  Gamepad2,
  Ghost,
  Globe2,
  Hammer,
  LayoutGrid,
  Map,
  Maximize2,
  MessageSquare,
  Mountain,
  Music2,
  PanelsTopLeft,
  PersonStanding,
  Radar,
  ScanEye,
  Shield,
  ShieldAlert,
  Sparkles,
  Sun,
  Sword,
  Target,
  UserRound,
  Volume2,
  Wand2,
  Wind,
  Zap,
  ZoomIn,
  type LucideIcon,
} from 'lucide-react';

export type Category = 'All modules' | 'Combat' | 'Movement' | 'Render' | 'Visuals' | 'Player' | 'World' | 'Misc' | 'Client';
export type View = 'modules' | 'appearance';
export type Mode = 'gui' | 'hud' | 'edit';
export type Filter = 'all' | 'enabled' | 'favorites';
export type AccentName = 'cyan' | 'acid' | 'ice' | 'melon';

export type ModuleSetting =
  | { id: string; type: 'toggle'; name: string; description: string; value: boolean }
  | { id: string; type: 'slider'; name: string; description: string; value: number; min: number; max: number; step: number }
  | { id: string; type: 'mode'; name: string; description: string; value: string; options: string[] };

export type ModuleDefinition = {
  id: string;
  name: string;
  description: string;
  category: Exclude<Category, 'All modules'>;
  icon: LucideIcon;
  keybind: string;
  enabled: boolean;
  favorite?: boolean;
  settings: ModuleSetting[];
};

export const modules: ModuleDefinition[] = [
  { id: 'aimassist', name: 'AimAssist', description: 'Smooth aim toward nearby targets.', category: 'Combat', icon: Target, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'How far it looks', value: 4.5, min: 1, max: 8, step: 0.1 },
    { id: 'speed', type: 'slider', name: 'Speed', description: 'How fast it turns', value: 35, min: 1, max: 100, step: 1 },
    { id: 'players', type: 'toggle', name: 'Players', description: 'Aim at players', value: true },
  ]},
  { id: 'autoclicker', name: 'AutoClicker', description: 'Clicks at a set CPS while holding.', category: 'Combat', icon: Zap, keybind: '', enabled: false, settings: [
    { id: 'cps', type: 'slider', name: 'CPS', description: 'Clicks per second', value: 12, min: 1, max: 20, step: 1 },
    { id: 'jitter', type: 'toggle', name: 'Jitter', description: 'Vary click timing', value: true },
  ]},
  { id: 'autocrystal', name: 'AutoCrystal', description: 'Places and breaks crystals automatically.', category: 'Combat', icon: Sparkles, keybind: '', enabled: false, settings: [
    { id: 'place', type: 'toggle', name: 'Place', description: 'Place crystals', value: true },
    { id: 'break', type: 'toggle', name: 'Break', description: 'Break crystals', value: true },
    { id: 'range', type: 'slider', name: 'Range', description: 'Reach in blocks', value: 4.5, min: 1, max: 6, step: 0.1 },
  ]},
  { id: 'autototem', name: 'Auto Totem', description: 'Keeps a totem in offhand or hotbar.', category: 'Combat', icon: Shield, keybind: '', enabled: false, favorite: true, settings: [
    { id: 'mode', type: 'mode', name: 'Mode', description: 'Where the totem goes', value: 'Offhand', options: ['Offhand', 'Hotbar', 'Both'] },
    { id: 'swap', type: 'toggle', name: 'Inventory swap', description: 'Pull from inventory if needed', value: true },
  ]},
  { id: 'autoinvtotem', name: 'Auto Inventory Totem', description: 'Moves totems from inventory when needed.', category: 'Combat', icon: ShieldAlert, keybind: '', enabled: false, settings: [
    { id: 'delay', type: 'slider', name: 'Delay', description: 'Ticks between swaps', value: 1, min: 0, max: 10, step: 1 },
  ]},
  { id: 'anchormacro', name: 'Anchor Macro', description: 'Charges and detonates respawn anchors.', category: 'Combat', icon: Hammer, keybind: '', enabled: false, settings: [
    { id: 'charge', type: 'toggle', name: 'Auto charge', description: 'Glowstone then detonate', value: true },
  ]},
  { id: 'doubleanchor', name: 'Double Anchor', description: 'Places a second anchor in sequence.', category: 'Combat', icon: Boxes, keybind: '', enabled: false, settings: [
    { id: 'delay', type: 'slider', name: 'Delay', description: 'Ticks between placements', value: 2, min: 0, max: 8, step: 1 },
  ]},
  { id: 'hitbox', name: 'HitBox', description: 'Expands entity hitboxes.', category: 'Combat', icon: Maximize2, keybind: '', enabled: false, settings: [
    { id: 'size', type: 'slider', name: 'Size', description: 'Hitbox scale', value: 1.2, min: 1, max: 3, step: 0.05 },
    { id: 'players', type: 'toggle', name: 'Players', description: 'Affect players', value: true },
  ]},
  { id: 'hovertotem', name: 'Hover Totem', description: 'Swaps a totem when hovering it.', category: 'Combat', icon: Shield, keybind: '', enabled: false, settings: [
    { id: 'hotbar', type: 'toggle', name: 'Hotbar only', description: 'Ignore inventory slots', value: false },
  ]},
  { id: 'macebomber', name: 'Mace Bomber', description: 'Times mace smash hits from height.', category: 'Combat', icon: Hammer, keybind: '', enabled: false, settings: [
    { id: 'minfall', type: 'slider', name: 'Min fall', description: 'Blocks of fall required', value: 3, min: 1, max: 20, step: 1 },
  ]},
  { id: 'maceswap', name: 'Mace Swap', description: 'Swaps to a mace for smash hits.', category: 'Combat', icon: Sword, keybind: '', enabled: false, settings: [
    { id: 'back', type: 'toggle', name: 'Swap back', description: 'Return after the hit', value: true },
  ]},
  { id: 'shieldbreaker', name: 'Shield Breaker', description: 'Axes shields on contact.', category: 'Combat', icon: ShieldAlert, keybind: '', enabled: false, settings: [
    { id: 'onlyaxe', type: 'toggle', name: 'Axe only', description: 'Require an axe in hand', value: true },
  ]},
  { id: 'triggerbot', name: 'Triggerbot', description: 'Clicks when a target is on crosshair.', category: 'Combat', icon: Crosshair, keybind: '', enabled: false, settings: [
    { id: 'delay', type: 'slider', name: 'Delay', description: 'Ms before click', value: 40, min: 0, max: 250, step: 5 },
    { id: 'players', type: 'toggle', name: 'Players', description: 'Click on players', value: true },
  ]},

  { id: 'autowalk', name: 'AutoWalk', description: 'Automatically walks forward.', category: 'Movement', icon: Footprints, keybind: 'G', enabled: false, settings: [
    { id: 'sprint', type: 'toggle', name: 'Sprint', description: 'Hold sprint while walking', value: true },
  ]},
  { id: 'freelook', name: 'FreeLook', description: 'Look around without changing move direction.', category: 'Movement', icon: ScanEye, keybind: 'V', enabled: false, settings: [
    { id: 'third', type: 'toggle', name: 'Third person', description: 'Force third person while held', value: true },
  ]},
  { id: 'freecam', name: 'Freecam', description: 'Detached camera. Body can keep walking.', category: 'Movement', icon: Camera, keybind: '', enabled: false, settings: [
    { id: 'speed', type: 'slider', name: 'Speed', description: 'Fly speed', value: 1.4, min: 0.2, max: 5, step: 0.1 },
    { id: 'body', type: 'toggle', name: 'Keep body', description: 'Leave the player body in place', value: true },
  ]},
  { id: 'elytraswap', name: 'Elytra Swap', description: 'Swaps chestplate and elytra on a key.', category: 'Movement', icon: Wind, keybind: '', enabled: false, settings: [
    { id: 'firework', type: 'toggle', name: 'Firework', description: 'Boost after swap', value: false },
  ]},
  { id: 'fastuse', name: 'FastUse', description: 'Speeds up item use and eating.', category: 'Movement', icon: Zap, keybind: '', enabled: false, settings: [
    { id: 'delay', type: 'slider', name: 'Delay', description: 'Ticks between uses', value: 0, min: 0, max: 4, step: 1 },
  ]},
  { id: 'swingspeed', name: 'Swing Speed', description: 'Changes how fast the arm swings.', category: 'Movement', icon: PersonStanding, keybind: '', enabled: false, settings: [
    { id: 'speed', type: 'slider', name: 'Speed', description: 'Swing multiplier', value: 1.4, min: 0.5, max: 3, step: 0.1 },
  ]},

  { id: 'fullbright', name: 'FullBright', description: 'Maximum brightness everywhere.', category: 'Render', icon: Sun, keybind: 'B', enabled: true, favorite: true, settings: [
    { id: 'gamma', type: 'slider', name: 'Gamma', description: 'Light level override', value: 15, min: 1, max: 16, step: 1 },
  ]},
  { id: 'playeresp', name: 'Player ESP', description: 'Highlights players through walls.', category: 'Render', icon: Eye, keybind: '', enabled: false, settings: [
    { id: 'box', type: 'toggle', name: 'Box', description: 'Draw a box', value: true },
    { id: 'tracers', type: 'toggle', name: 'Tracers', description: 'Draw tracers', value: false },
  ]},
  { id: 'mobesp', name: 'Mob ESP', description: 'Highlights mobs in range.', category: 'Render', icon: Ghost, keybind: '', enabled: false, settings: [
    { id: 'hostile', type: 'toggle', name: 'Hostile', description: 'Show hostile mobs', value: true },
    { id: 'passive', type: 'toggle', name: 'Passive', description: 'Show passive mobs', value: false },
  ]},
  { id: 'blockesp', name: 'Block ESP', description: 'Highlights selected blocks.', category: 'Render', icon: Box, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'Scan radius', value: 48, min: 8, max: 96, step: 8 },
  ]},
  { id: 'blockentityesp', name: 'Block Entity ESP', description: 'Highlights chests, spawners, and more.', category: 'Render', icon: Boxes, keybind: '', enabled: false, settings: [
    { id: 'chests', type: 'toggle', name: 'Chests', description: 'Show chests', value: true },
    { id: 'spawners', type: 'toggle', name: 'Spawners', description: 'Show spawners', value: true },
  ]},
  { id: 'storageesp', name: 'Storage ESP', description: 'Highlights storage containers.', category: 'Render', icon: Box, keybind: '', enabled: false, settings: [
    { id: 'shulkers', type: 'toggle', name: 'Shulkers', description: 'Show shulker boxes', value: true },
  ]},
  { id: 'blockoutline', name: 'Block Outline', description: 'Custom block outline color and width.', category: 'Render', icon: Aperture, keybind: '', enabled: false, settings: [
    { id: 'width', type: 'slider', name: 'Width', description: 'Outline thickness', value: 2, min: 1, max: 6, step: 0.5 },
  ]},
  { id: 'nametags', name: 'NameTags', description: 'Renders extra nametag info in world.', category: 'Render', icon: UserRound, keybind: '', enabled: false, settings: [
    { id: 'health', type: 'toggle', name: 'Health', description: 'Show health', value: true },
    { id: 'items', type: 'toggle', name: 'Items', description: 'Show held items', value: true },
  ]},
  { id: 'regionmap', name: 'RegionMap', description: 'DonutSMP server region map on the HUD.', category: 'Render', icon: Map, keybind: '', enabled: false, settings: [
    { id: 'scale', type: 'slider', name: 'Scale', description: 'Map size', value: 1, min: 0.5, max: 2, step: 0.1 },
  ]},
  { id: 'holeesp', name: 'Hole ESP', description: 'Marks safe holes nearby.', category: 'Render', icon: Mountain, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'Scan radius', value: 16, min: 4, max: 32, step: 1 },
  ]},
  { id: 'jumpcircles', name: 'Jump Circles', description: 'Draws a ring when you jump.', category: 'Render', icon: Aperture, keybind: '', enabled: false, settings: [
    { id: 'life', type: 'slider', name: 'Lifetime', description: 'How long rings last', value: 0.8, min: 0.2, max: 2, step: 0.1 },
  ]},
  { id: 'hitparticles', name: 'Hit Particles', description: 'Extra particles on hit.', category: 'Render', icon: Sparkles, keybind: '', enabled: false, settings: [
    { id: 'amount', type: 'slider', name: 'Amount', description: 'Particles per hit', value: 8, min: 1, max: 24, step: 1 },
  ]},
  { id: 'motionblur', name: 'Motion Blur', description: 'Adds a subtle motion trail.', category: 'Render', icon: Aperture, keybind: '', enabled: false, settings: [
    { id: 'amount', type: 'slider', name: 'Amount', description: 'Blur strength', value: 0.35, min: 0.05, max: 1, step: 0.05 },
  ]},
  { id: 'customglint', name: 'Custom Glint', description: 'Replaces enchanted item glint.', category: 'Render', icon: Wand2, keybind: '', enabled: false, settings: [
    { id: 'style', type: 'mode', name: 'Style', description: 'Glint texture', value: 'Cyber', options: ['Cyber', 'Abyss', 'Galaxy', 'Solar', 'Toxic', 'Void'] },
  ]},
  { id: 'customaccessories', name: 'Custom Accessories', description: 'Cosmetic trails and extras.', category: 'Render', icon: Sparkles, keybind: '', enabled: false, settings: [
    { id: 'trail', type: 'toggle', name: 'Trail', description: 'Show a movement trail', value: true },
  ]},
  { id: 'armortrimhider', name: 'Armor Trim Hider', description: 'Hides armor trims on players.', category: 'Render', icon: Shield, keybind: '', enabled: false, settings: [
    { id: 'self', type: 'toggle', name: 'Self', description: 'Hide on yourself too', value: true },
  ]},
  { id: 'spawnertags', name: 'Spawner Nametags', description: 'Labels spawners in the world.', category: 'Render', icon: Box, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'Visible distance', value: 32, min: 8, max: 64, step: 4 },
  ]},

  { id: 'crosshair', name: 'CustomCrosshair', description: 'Draws a custom crosshair.', category: 'Visuals', icon: Crosshair, keybind: '', enabled: true, favorite: true, settings: [
    { id: 'style', type: 'mode', name: 'Style', description: 'Crosshair shape', value: 'Plus', options: ['Plus', 'Dot', 'Cross', 'Circle'] },
    { id: 'size', type: 'slider', name: 'Size', description: 'Pixel size', value: 8, min: 2, max: 16, step: 1 },
  ]},
  { id: 'customfov', name: 'Custom FOV', description: 'Locks or overrides field of view.', category: 'Visuals', icon: Aperture, keybind: '', enabled: false, settings: [
    { id: 'fov', type: 'slider', name: 'FOV', description: 'Field of view', value: 90, min: 30, max: 150, step: 1 },
  ]},
  { id: 'zoom', name: 'Zoom', description: 'Optical zoom on a key.', category: 'Visuals', icon: ZoomIn, keybind: 'C', enabled: true, settings: [
    { id: 'amount', type: 'slider', name: 'Amount', description: 'Zoom multiplier', value: 4, min: 2, max: 10, step: 0.5 },
    { id: 'smooth', type: 'toggle', name: 'Smooth', description: 'Ease in and out', value: true },
  ]},

  { id: 'nameprotect', name: 'NameProtect', description: 'Hides player names in clips.', category: 'Player', icon: UserRound, keybind: '', enabled: false, settings: [
    { id: 'self', type: 'toggle', name: 'Self', description: 'Hide your own name', value: true },
    { id: 'others', type: 'toggle', name: 'Others', description: 'Hide other names', value: true },
  ]},
  { id: 'skinprotect', name: 'Skin Protect', description: 'Hides or replaces player skins.', category: 'Player', icon: Ghost, keybind: '', enabled: false, settings: [
    { id: 'mode', type: 'mode', name: 'Mode', description: 'How skins are hidden', value: 'Steve', options: ['Steve', 'Blank', 'Self'] },
  ]},
  { id: 'fakepay', name: 'Fake Pay', description: 'Spoofs pay messages in chat.', category: 'Player', icon: MessageSquare, keybind: '', enabled: false, settings: [
    { id: 'amount', type: 'slider', name: 'Amount', description: 'Shown amount', value: 100, min: 1, max: 10000, step: 1 },
  ]},
  { id: 'fakeroles', name: 'Fake Roles', description: 'Spoofs tab and nametag roles.', category: 'Player', icon: UserRound, keybind: '', enabled: false, settings: [
    { id: 'role', type: 'mode', name: 'Role', description: 'Displayed rank', value: 'Default', options: ['Default', 'VIP', 'Mod', 'Admin'] },
  ]},
  { id: 'fakestats', name: 'Fake Stats', description: 'Spoofs displayed player stats.', category: 'Player', icon: Target, keybind: '', enabled: false, settings: [
    { id: 'kills', type: 'slider', name: 'Kills', description: 'Shown kill count', value: 0, min: 0, max: 9999, step: 1 },
  ]},
  { id: 'autotpa', name: 'Auto TPA', description: 'Accepts teleport requests automatically.', category: 'Player', icon: PersonStanding, keybind: '', enabled: false, settings: [
    { id: 'friends', type: 'toggle', name: 'Friends only', description: 'Only accept friends', value: true },
  ]},
  { id: 'chatmacro', name: 'Chat Macro', description: 'Sends saved chat lines on a key.', category: 'Player', icon: MessageSquare, keybind: '', enabled: false, settings: [
    { id: 'delay', type: 'slider', name: 'Delay', description: 'Ms between lines', value: 250, min: 0, max: 2000, step: 50 },
  ]},

  { id: 'chunkfinder', name: 'Chunk Finder', description: 'Flags interesting chunks around you.', category: 'World', icon: Radar, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'Chunk radius', value: 8, min: 2, max: 16, step: 1 },
  ]},
  { id: 'suschunk', name: 'Sus Chunk Finder', description: 'Highlights suspicious generated chunks.', category: 'World', icon: Radar, keybind: '', enabled: false, settings: [
    { id: 'fade', type: 'slider', name: 'Fade', description: 'How long marks stay', value: 8, min: 1, max: 30, step: 1 },
  ]},
  { id: 'coordsnapper', name: 'Coord Snapper', description: 'Copies and snaps coordinates.', category: 'World', icon: Map, keybind: '', enabled: false, settings: [
    { id: 'chat', type: 'toggle', name: 'Chat', description: 'Print coords in chat', value: true },
  ]},
  { id: 'weather', name: 'WeatherNotifier', description: 'Themed toast when the weather changes.', category: 'World', icon: CloudSun, keybind: '', enabled: false, settings: [
    { id: 'sound', type: 'toggle', name: 'Sound', description: 'Play a notify sound', value: true },
  ]},
  { id: 'spawnerprotect', name: 'Spawner Protect', description: 'Warns when spawners are broken nearby.', category: 'World', icon: ShieldAlert, keybind: '', enabled: false, settings: [
    { id: 'range', type: 'slider', name: 'Range', description: 'Listen radius', value: 24, min: 8, max: 64, step: 4 },
  ]},
  { id: 'stafflist', name: 'Staff List', description: 'Tracks staff on the current server.', category: 'World', icon: UserRound, keybind: '', enabled: false, settings: [
    { id: 'tab', type: 'toggle', name: 'Tab', description: 'Scan tab list', value: true },
  ]},
  { id: 'sounddebug', name: 'Sound Debug', description: 'Shows recent world sounds on HUD.', category: 'World', icon: Volume2, keybind: '', enabled: false, settings: [
    { id: 'limit', type: 'slider', name: 'Limit', description: 'Rows on HUD', value: 8, min: 3, max: 20, step: 1 },
  ]},

  { id: 'gamblerigger', name: 'Gamble Rigger', description: 'Overlay for gambling minigames.', category: 'Misc', icon: Dice5, keybind: '', enabled: false, settings: [
    { id: 'overlay', type: 'toggle', name: 'Overlay', description: 'Show the helper overlay', value: true },
  ]},

  { id: 'hud', name: 'HUD', description: 'Watermark, arraylist, coords, and extras.', category: 'Client', icon: PanelsTopLeft, keybind: '', enabled: true, favorite: true, settings: [
    { id: 'watermark', type: 'toggle', name: 'Watermark', description: 'Show Watermalon mark', value: true },
    { id: 'arraylist', type: 'toggle', name: 'ArrayList', description: 'Show enabled modules', value: true },
    { id: 'coords', type: 'toggle', name: 'Coordinates', description: 'Show XYZ', value: true },
  ]},
  { id: 'clickgui', name: 'ClickGUI', description: 'This menu. Right Shift by default.', category: 'Client', icon: LayoutGrid, keybind: 'RSHIFT', enabled: true, settings: [
    { id: 'blur', type: 'toggle', name: 'Menu blur', description: 'Blur the world behind the GUI', value: true },
    { id: 'sounds', type: 'toggle', name: 'UI sounds', description: 'Play click sounds', value: true },
  ]},
  { id: 'spotify', name: 'SpotifyHUD', description: 'Now playing card and queue.', category: 'Client', icon: Music2, keybind: '', enabled: true, favorite: true, settings: [
    { id: 'controls', type: 'toggle', name: 'Controls', description: 'Prev / play / next on the card', value: true },
    { id: 'queue', type: 'toggle', name: 'Queue', description: 'Show the live queue', value: true },
    { id: 'idle', type: 'toggle', name: 'Hide when idle', description: 'Hide the card when nothing plays', value: false },
  ]},
  { id: 'waterplus', name: 'Watermalon+', description: 'Accent, glass, roundness, and GUI key.', category: 'Client', icon: Gamepad2, keybind: '', enabled: true, settings: [
    { id: 'glass', type: 'slider', name: 'Glass', description: 'Glass intensity', value: 29, min: 0, max: 100, step: 1 },
    { id: 'round', type: 'slider', name: 'Roundness', description: 'Corner radius', value: 10, min: 0, max: 20, step: 1 },
  ]},
];

export const categories: { label: Category; icon: LucideIcon }[] = [
  { label: 'All modules', icon: LayoutGrid },
  { label: 'Combat', icon: Sword },
  { label: 'Movement', icon: Wind },
  { label: 'Render', icon: Eye },
  { label: 'Visuals', icon: Aperture },
  { label: 'Player', icon: UserRound },
  { label: 'World', icon: Globe2 },
  { label: 'Misc', icon: Dice5 },
  { label: 'Client', icon: Gamepad2 },
];

export const accents: { id: AccentName; label: string; description: string; color: string; rgb: string; icon: LucideIcon }[] = [
  { id: 'melon', label: 'Melon pink', description: 'The Watermalon look.', color: '#ff6b8a', rgb: '255, 107, 138', icon: Sparkles },
  { id: 'cyan', label: 'Cyber cyan', description: 'Cool, calm, focused.', color: '#70eadb', rgb: '112, 234, 219', icon: Sparkles },
  { id: 'acid', label: 'Electric acid', description: 'A little more energy.', color: '#c9f36b', rgb: '201, 243, 107', icon: Sparkles },
  { id: 'ice', label: 'Polar ice', description: 'Clean and understated.', color: '#b7d8e8', rgb: '183, 216, 232', icon: Sparkles },
];

export type Track = { title: string; artist: string; duration: number };
export const tracks: Track[] = [
  { title: 'Night Drive', artist: 'Sora', duration: 224 },
  { title: 'Afterglow', artist: 'Everlight', duration: 198 },
  { title: 'Daydream', artist: 'Luma', duration: 235 },
];

export type HudOptions = {
  artwork: boolean;
  progress: boolean;
  glass: boolean;
  watermark: boolean;
  moduleList: boolean;
  info: boolean;
  keystrokes: boolean;
};

export const defaultHudOptions: HudOptions = {
  artwork: true,
  progress: true,
  glass: true,
  watermark: true,
  moduleList: true,
  info: true,
  keystrokes: true,
};

export type HudElementId = 'watermark' | 'arraylist' | 'spotify' | 'info' | 'keystrokes';
export type HudPoint = { x: number; y: number };
export type HudLayout = Record<HudElementId, HudPoint>;

// Normalized positions (0..1) of the free space, like the client's fx/fy.
export const defaultHudLayout: HudLayout = {
  watermark: { x: 0.012, y: 0.02 },
  arraylist: { x: 0.988, y: 0.02 },
  spotify: { x: 0.012, y: 0.86 },
  info: { x: 0.012, y: 0.985 },
  keystrokes: { x: 0.988, y: 0.975 },
};

export const hudElements: { id: HudElementId; label: string }[] = [
  { id: 'watermark', label: 'Watermark' },
  { id: 'arraylist', label: 'ArrayList' },
  { id: 'spotify', label: 'Spotify' },
  { id: 'info', label: 'Info' },
  { id: 'keystrokes', label: 'Keystrokes' },
];

export type WindowRect = { x: number; y: number; w: number; h: number };

export const defaultEnabled = Object.fromEntries(modules.map((module) => [module.id, module.enabled])) as Record<string, boolean>;
export const defaultFavorites = Object.fromEntries(modules.map((module) => [module.id, Boolean(module.favorite)])) as Record<string, boolean>;
export const defaultKeybinds = Object.fromEntries(modules.map((module) => [module.id, module.keybind])) as Record<string, string>;
export const defaultSettings = Object.fromEntries(modules.map((module) => [module.id, module.settings])) as Record<string, ModuleSetting[]>;
