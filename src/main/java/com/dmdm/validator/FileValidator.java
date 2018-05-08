package com.dmdm.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.dmdm.model.UploadedFile;

public class FileValidator implements Validator {

	@Override
	public boolean supports(Class<?> arg0) {
		return false;
	}

	@Override
	public void validate(Object uploadedFile, Errors errors) {

		UploadedFile file = (UploadedFile) uploadedFile;

		if (file.getFile().getSize() == 0) {
			errors.rejectValue("file", "uploadForm.selectFile",
					"Please select a file!");
		}else if(!file.getFile().getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			errors.rejectValue("file", "uploadForm.selectFile",
					"Incorrect file type. Please upload in .xls format !");
		}
		

	}

}
