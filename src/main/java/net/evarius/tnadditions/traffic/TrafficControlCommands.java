package net.evarius.tnadditions.traffic;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.evarius.tnadditions.block.custom.DigitalTrafficDisplayBlock;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Central, server-authoritative control commands for every registered traffic device. */
public final class TrafficControlCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                literal("tntraffic").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("list").executes(context -> list(context.getSource())))
                        .then(literal("program").then(argument("id", StringArgumentType.word())
                                .then(argument("phases", StringArgumentType.greedyString()).executes(context -> program(context.getSource(),
                                        StringArgumentType.getString(context,"id"),StringArgumentType.getString(context,"phases"))))))
                        .then(literal("template").then(argument("id", StringArgumentType.word())
                                .then(argument("mode", StringArgumentType.word())
                                .then(argument("value", IntegerArgumentType.integer(0, 999))
                                .then(argument("text", StringArgumentType.greedyString()).executes(context -> template(
                                        context.getSource(), StringArgumentType.getString(context, "id"),
                                        StringArgumentType.getString(context, "mode"), IntegerArgumentType.getInteger(context, "value"),
                                        StringArgumentType.getString(context, "text"))))))))
                        .then(literal("device").then(argument("position", BlockPosArgumentType.blockPos())
                                .then(literal("group").then(argument("group",StringArgumentType.word()).executes(context -> group(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"group")))))
                                .then(literal("area").then(argument("area",StringArgumentType.word()).executes(context -> area(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"area")))))
                                .then(literal("mode").then(argument("mode",StringArgumentType.word()).executes(context -> mode(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"mode")))))
                                .then(literal("value").then(argument("value",IntegerArgumentType.integer(0,999)).executes(context -> value(context.getSource(),pos(context.getSource(),context,"position"),IntegerArgumentType.getInteger(context,"value")))))
                                .then(literal("text").then(argument("text",StringArgumentType.greedyString()).executes(context -> text(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"text")))))
                                .then(literal("enabled").then(argument("enabled",BoolArgumentType.bool()).executes(context -> enabled(context.getSource(),pos(context.getSource(),context,"position"),BoolArgumentType.getBool(context,"enabled")))))
                                .then(literal("signal_program").then(argument("program",StringArgumentType.word()).executes(context -> signalProgram(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"program")))))
                                .then(literal("manual").then(argument("aspect",StringArgumentType.word()).executes(context -> manual(context.getSource(),pos(context.getSource(),context,"position"),StringArgumentType.getString(context,"aspect")))))
                                .then(literal("automatic").executes(context -> automatic(context.getSource(),pos(context.getSource(),context,"position"))))
                                .then(literal("apply_template").then(argument("template", StringArgumentType.word())
                                        .executes(context -> applyTemplate(context.getSource(), pos(context.getSource(), context, "position"),
                                                StringArgumentType.getString(context, "template")))))
                                .then(literal("apply_group").executes(context -> applyGroup(context.getSource(),pos(context.getSource(),context,"position"))))))));
    }

    private static BlockPos pos(ServerCommandSource source, com.mojang.brigadier.context.CommandContext<ServerCommandSource> context, String name) {
        try { return BlockPosArgumentType.getLoadedBlockPos(context,name); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { return BlockPos.ofFloored(source.getPosition()); }
    }
    private static TrafficDevice device(ServerCommandSource source, BlockPos pos) {
        return TrafficControlState.get(source.getServer()).device(source.getWorld(),pos);
    }
    private static int list(ServerCommandSource source){var all=TrafficControlState.get(source.getServer()).devices();source.sendFeedback(()->Text.literal("Traffic devices: "+all.size()),false);all.stream().limit(20).forEach(d->source.sendFeedback(()->Text.literal(d.type()+" "+d.dimension()+" "+d.blockPos().toShortString()+" group="+d.groupId()),false));return all.size();}
    private static int group(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withRouting(v,d.areaId()));return ok(s);}
    private static int area(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withRouting(d.groupId(),v));return ok(s);}
    private static int mode(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficDevice u=d.withDisplay(TrafficDisplayMode.parse(v),d.displayValue(),d.displayText(),d.enabled());TrafficControlState.get(s.getServer()).update(u);syncDisplay(s,p,u);return ok(s);}
    private static int value(ServerCommandSource s,BlockPos p,int v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withDisplay(TrafficDisplayMode.parse(d.displayMode()),v,d.displayText(),d.enabled()));return ok(s);}
    private static int text(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withDisplay(TrafficDisplayMode.TEXT,d.displayValue(),v,d.enabled()));syncDisplay(s,p,TrafficControlState.get(s.getServer()).device(s.getWorld(),p));return ok(s);}
    private static int enabled(ServerCommandSource s,BlockPos p,boolean v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withDisplay(TrafficDisplayMode.parse(d.displayMode()),d.displayValue(),d.displayText(),v));return ok(s);}
    private static int signalProgram(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState state=TrafficControlState.get(s.getServer());if(state.program(v)==null)return missing(s);state.update(d.withProgram(v,false,TrafficSignalAspect.parse(d.manualAspect())));return ok(s);}
    private static int manual(ServerCommandSource s,BlockPos p,String v){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withProgram(d.programId(),true,TrafficSignalAspect.parse(v)));return ok(s);}
    private static int automatic(ServerCommandSource s,BlockPos p){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState.get(s.getServer()).update(d.withProgram(d.programId(),false,TrafficSignalAspect.parse(d.manualAspect())));return ok(s);}
    private static int applyGroup(ServerCommandSource s,BlockPos p){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState state=TrafficControlState.get(s.getServer());state.applyGroup(d);return ok(s);}
    private static int template(ServerCommandSource s,String id,String mode,int value,String text){TrafficControlState.get(s.getServer()).putTemplate(new TrafficDisplayTemplate(id,mode,value,text,true));return ok(s);}
    private static int applyTemplate(ServerCommandSource s,BlockPos p,String id){TrafficDevice d=device(s,p);if(d==null)return missing(s);TrafficControlState state=TrafficControlState.get(s.getServer());TrafficDisplayTemplate template=state.template(id);if(template==null)return missing(s);TrafficDevice updated=d.withDisplay(TrafficDisplayMode.parse(template.mode()),template.value(),template.text(),template.enabled());state.update(updated);syncDisplay(s,p,updated);return ok(s);}
    private static int program(ServerCommandSource s,String id,String specification){List<TrafficPhase> phases=new ArrayList<>();for(String token:specification.split(",")){String[] pair=token.trim().split(":",2);if(pair.length!=2)continue;try{phases.add(new TrafficPhase(TrafficSignalAspect.parse(pair[0]).asString(),Math.max(1,Integer.parseInt(pair[1].trim()))*20));}catch(NumberFormatException ignored){}}if(phases.isEmpty()){s.sendError(Text.literal("Invalid phases. Example: red:10,green:12,yellow:3"));return 0;}TrafficControlState.get(s.getServer()).putProgram(new TrafficProgram(id,id,phases));return ok(s);}
    private static void syncDisplay(ServerCommandSource s,BlockPos p,TrafficDevice d){var state=s.getWorld().getBlockState(p);if(d!=null&&state.contains(DigitalTrafficDisplayBlock.MODE))s.getWorld().setBlockState(p,state.with(DigitalTrafficDisplayBlock.MODE,TrafficDisplayMode.parse(d.displayMode())),Block.NOTIFY_LISTENERS);}
    private static int missing(ServerCommandSource s){s.sendError(Text.literal("No registered traffic device at this position."));return 0;}
    private static int ok(ServerCommandSource s){s.sendFeedback(()->Text.literal("Traffic control updated."),false);return 1;}
    private TrafficControlCommands(){}
}
