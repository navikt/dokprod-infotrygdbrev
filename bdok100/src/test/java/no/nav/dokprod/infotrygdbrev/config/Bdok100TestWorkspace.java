package no.nav.dokprod.infotrygdbrev.config;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Bdok100TestWorkspace implements AutoCloseable {

	private final Path root;
	private final Path input;
	private final Path staticInput;
	private final Path output;
	private final Path behandlet;
	private final Path failed;

	public Bdok100TestWorkspace() throws IOException {
		root = Files.createTempDirectory("bdok100-itest-");
		input = root.resolve("input");
		staticInput = input.resolve("static");
		output = root.resolve("output");
		behandlet = root.resolve("behandlet");
		failed = root.resolve("failed");
		reset();
	}

	public void reset() throws IOException {
		FileUtils.cleanDirectory(root.toFile());
		Files.createDirectories(staticInput);
		Files.createDirectories(output);
		Files.createDirectories(behandlet);
		Files.createDirectories(failed);
	}

	public Path input() {
		return input;
	}

	public Path staticInput() {
		return staticInput;
	}

	public Path output() {
		return output;
	}

	public Path behandlet() {
		return behandlet;
	}

	public Path failed() {
		return failed;
	}

	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(root.toFile());
	}
}
