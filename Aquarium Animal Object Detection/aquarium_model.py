import torch
import torchvision
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
import torchvision.transforms as T

from engine import *
import utils
import transforms as T

from PIL import Image
import matplotlib
import matplotlib.patches as patches
import matplotlib.pyplot as plt

from aquarium_dataset import AquariumDataset

import wandb

# HYPERPARAMETERS

BATCH_SIZE = 1
NUM_CLASSES = 8
LEARNING_RATE = .004
NUM_EPOCHS = 2
MODEL_STORE_PATH = 'C:/Users/aaron/OneDrive/Documents/PERSONAL DOCUMENTS/Private-Projects/Learning ML/Aquarium Classify/test_model.pt'

# Sending to Weights and Biases

wandb.init(project="Aquarium Animal Detection", entity="aaronsoll")
config = wandb.config
config.learning_rate = LEARNING_RATE
config.batch_size = BATCH_SIZE
config.num_epochs = NUM_EPOCHS


# define training and test datasets and loaders
dataset = AquariumDataset('train', get_transform(train=True))
dataset_test = AquariumDataset('test', get_transform(train=False))

data_loader = torch.utils.data.DataLoader(
    dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=0,
    collate_fn=utils.collate_fn)

data_loader_test = torch.utils.data.DataLoader(
    dataset_test, batch_size=BATCH_SIZE, shuffle=False, num_workers=0,
    collate_fn=utils.collate_fn)


device = torch.device(
    'cuda') if torch.cuda.is_available() else torch.device('cpu')

# get the model using our helper function
# model = get_instance_segmentation_model(NUM_CLASSES)
model = get_box_model(NUM_CLASSES)
model.to(device)

# construct an optimizer
params = [p for p in model.parameters() if p.requires_grad]
optimizer = torch.optim.SGD(params, lr=LEARNING_RATE,
                            momentum=0.9, weight_decay=0.0005)

# and a learning rate scheduler which decreases the learning rate by
# 10x every 3 epochs
lr_scheduler = torch.optim.lr_scheduler.StepLR(optimizer,
                                               step_size=3,
                                               gamma=0.1)

# training loop
wandb.watch(model)
for epoch in range(NUM_EPOCHS):
    # train for one epoch, printing every 10 iterations
    train_one_epoch(model, optimizer, data_loader,
                    device, epoch, print_freq=25)
    # update the learning rate
    lr_scheduler.step()
    # evaluate on the test dataset
    evaluate(model, data_loader_test, device=device)


torch.save(model.state_dict(), MODEL_STORE_PATH)
