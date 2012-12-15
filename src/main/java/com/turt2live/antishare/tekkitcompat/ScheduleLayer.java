package com.turt2live.antishare.tekkitcompat;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.turt2live.antishare.tekkitcompat.task.AntiShareTask;
import com.turt2live.antishare.tekkitcompat.task.CB145Task;
import com.turt2live.antishare.tekkitcompat.task.MC125Task;

public class ScheduleLayer {

	@SuppressWarnings ("deprecation")
	public static AntiShareTask runTaskAsynchronously(Plugin plugin, Runnable runnable){
		if(!ServerHas.runTaskMethod()){
			int id = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, runnable);
			return new MC125Task(id);
		}else{
			BukkitTask task = plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
			return new CB145Task(task);
		}
	}

	@SuppressWarnings ("deprecation")
	public static AntiShareTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period){
		if(!ServerHas.runTaskMethod()){
			int id = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, runnable, delay, period);
			return new MC125Task(id);
		}else{
			BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
			return new CB145Task(task);
		}
	}

}