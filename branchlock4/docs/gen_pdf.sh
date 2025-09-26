#!/bin/bash

# This file is used to generate the PDFs for the documentation using pandoc.
# It first ensures pandoc is installed using the package manager.
# Then, all markdown files in the docs folder are converted to PDFs using pandoc.
# The title is specified using the pandoc command. It corresponds to the file name.
# The output is saved in the docs/pdf folder.

# Check if pandoc is installed
if ! command -v pandoc &> /dev/null
then
    echo "pandoc could not be found. Please install pandoc using your package manager."
    exit
fi

# Convert all markdown files to PDFs
for file in *.md
do
    filename=$(basename -- "$file")
    filename="${filename%.*}"
    echo "Generating PDF for $filename"
    title="${filename//_/ }"

    pandoc "$file" -o "pdf/$filename.pdf" -V title="$title" -V geometry:margin=1.5in
done

