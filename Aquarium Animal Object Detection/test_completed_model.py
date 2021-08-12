import torch
import torchvision
from engine import get_box_model, get_transform, evaluate
from aquarium_dataset import AquariumDataset
import utils
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from PIL import Image
from torchvision import transforms as T
import tkinter
import os
import random
from torchvision.transforms.functional import convert_image_dtype

from visual_utils import *

NUM_CLASSES = 8
TEST_MODEL_PATH = "test_model.pt"

# matplotlib.use('TkAgg')

test_model = get_box_model(NUM_CLASSES)
test_model.load_state_dict(torch.load(TEST_MODEL_PATH))
test_model.eval()
test_model.to(torch.device('cuda'))

images = []
for pic in list(os.listdir(os.path.join('train', "Images"))):
    images += [torchvision.io.read_image(os.path.join('train', 'Images', pic))]

with torch.no_grad():
    while (True):
        batch_int = torch.stack([random.choice(images)])
        batch = convert_image_dtype(batch_int, dtype=torch.float)
        batch = batch.cuda()
        outputs = test_model(batch)
        print("predictions are:", outputs[0]["labels"],
              "\nscores are:", outputs[0]['scores'])
        plot_best_boxes(.25, outputs, batch_int)
