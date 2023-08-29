package org.mobilizadores.ccmp.support.files;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InputFile {

	/**
	 * The root directory of the target file, arbitrarily defined
	 */
	private File baseDirectory;
	
	private File targetFile;
}
