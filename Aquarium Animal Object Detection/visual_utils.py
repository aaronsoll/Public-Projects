import torch
import numpy as np
import matplotlib.pyplot as plt

import torchvision.transforms.functional as F
from torchvision.utils import draw_bounding_boxes


plt.rcParams["savefig.bbox"] = 'tight'


def show(imgs):
    if not isinstance(imgs, list):
        imgs = [imgs]
    fix, axs = plt.subplots(ncols=len(imgs), squeeze=False)
    for i, img in enumerate(imgs):
        img = img.detach()
        img = F.to_pil_image(img)
        axs[0, i].imshow(np.asarray(img))
        axs[0, i].set(xticklabels=[], yticklabels=[], xticks=[], yticks=[])
    plt.show()


def bounding_boxes(img, boxes, colors):
    show(draw_bounding_boxes(img, boxes, colors=colors, width=2))


def plot_best_boxes(score_thresh, outputs, imgs):
    # imgs is a stack of torch tensor rgb images
    # outputs is a the output of model(batch)

    imgs_and_boxes = [draw_bounding_boxes(
        img, boxes=output['boxes'][output['scores'] > score_thresh], width=2) for img, output in zip(imgs, outputs)]
    show(imgs_and_boxes)
