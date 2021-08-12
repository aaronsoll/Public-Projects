import os
import numpy as np
import torch
import torch.utils.data
from PIL import Image
import csv


class AquariumDataset(torch.utils.data.Dataset):
    def __init__(self, root, transforms=None):
        self.root = root
        self.transforms = transforms
        self.imgs = list(sorted(os.listdir(os.path.join(root, "Images"))))

    def __getitem__(self, idx):
        img_name = self.imgs[idx]
        img_path = os.path.join(self.root, "Images", img_name)
        img = Image.open(img_path).convert("RGB")

        # reads csv file and finds the row of the appropriate image
        with open(os.path.join(self.root, '_annotations.csv')) as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                if row["filename"] == img_name:
                    image_info = row
                    break

        boxes = [[int(image_info["xmin"]), int(image_info["ymin"]),
                  int(image_info["xmax"]), int(image_info["ymax"])]]
        boxes = torch.as_tensor(boxes, dtype=torch.float32)

        labels_key = {"fish": 1, "jellyfish": 2, "penguin": 3,
                      "shark": 4, "puffin": 5, "stingray": 6, "starfish": 7}
        labels = [labels_key[image_info["class"]]]
        labels = torch.tensor(labels, dtype=torch.int64)

        image_id = torch.tensor([idx])

        area = (boxes[:, 3] - boxes[:, 1]) * (boxes[:, 2] - boxes[:, 0])

        iscrowd = torch.tensor([0], dtype=torch.int64)

        target = {}
        target["boxes"] = boxes
        target["labels"] = labels
        target["image_id"] = image_id
        target["area"] = area
        target["iscrowd"] = iscrowd

        if self.transforms is not None:
            img, target = self.transforms(img, target)

        return img, target

    def __len__(self):
        return len(self.imgs)
