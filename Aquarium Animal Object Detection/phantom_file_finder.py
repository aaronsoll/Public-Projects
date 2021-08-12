import os
import csv

files_in_csv = set()

with open(os.path.join("train", '_annotations.csv')) as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        files_in_csv.add(row["filename"])

imgs = list(sorted(os.listdir(os.path.join("train", "Images"))))
for img in imgs:
    if img not in files_in_csv:
        print(img)
