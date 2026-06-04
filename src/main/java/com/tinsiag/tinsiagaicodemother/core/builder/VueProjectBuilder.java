package com.tinsiag.tinsiagaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {

    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vueProjectBuilder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("异步构建 Vue 项目失败，项目路径：{}, 错误信息：{}", projectPath, e.getMessage());
                    }
                });
    }

    public boolean buildProject(String projectPath) {
        File file = new File(projectPath);
        if (!file.exists() || !file.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }
        File packageJson = new File(file, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在：{}", packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建 Vue 项目，项目路径：{}", projectPath);
        if (!executeNpmInstall(file)) {
            log.error("npm install 失败");
            return false;
        }
        log.info("npm install 成功");
        if (!executeNpmBuild(file)) {
            log.error("npm run build 失败");
            return false;
        }
        File dist = new File(file, "dist");
        if (!dist.exists() || !dist.isDirectory()) {
            log.error("构建完成，但 dist 目录不存在：{}", dist.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录路径：{}", dist.getAbsolutePath());
        return true;
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildeCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildeCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private String buildeCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }

    }

}
